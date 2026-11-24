package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@TeleOp
public class LinearServoDemo extends OpMode {
    // Drive motors
    DcMotor frontLeftDrive;
    DcMotor frontRightDrive;
    DcMotor backLeftDrive;
    DcMotor backRightDrive;

    // Mechanism motors
    DcMotor flywheel;
    DcMotor intake;

    // Servos
    Servo feed1;
    Servo feed2;
    Servo pusher;
    Servo lift;

    // NEW: linear servo
    Servo linearServo;

    // IMU
    IMU imu;

    boolean intakeOn = false;
    int launchState = 0;
    double speedMult = 1;
    double flywheelSpeed = 0;
    double lServoPos = 0.05;

    private ElapsedTime launchStateTime = new ElapsedTime();

    @Override
    public void init() {
        frontLeftDrive = hardwareMap.get(DcMotor.class, "lf");
        frontRightDrive = hardwareMap.get(DcMotor.class, "rf");
        backLeftDrive = hardwareMap.get(DcMotor.class, "lr");
        backRightDrive = hardwareMap.get(DcMotor.class, "rr");
        flywheel = hardwareMap.get(DcMotor.class, "flywheel");
        intake = hardwareMap.get(DcMotor.class, "intake");

        feed1 = hardwareMap.get(Servo.class, "feed 1");
        feed2 = hardwareMap.get(Servo.class, "feed 2");
        pusher = hardwareMap.get(Servo.class, "pusher");
        lift = hardwareMap.get(Servo.class, "lift");

        // NEW: map linear servo (must match RC config name exactly)
        linearServo = hardwareMap.get(Servo.class, "ramp");

        // NEW: constrain servo range (tune as needed)
        linearServo.scaleRange(0.05, 0.95);

        // Reverse left motors
        backLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);

        // Encoder modes (optional)
        frontLeftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backLeftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        flywheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        imu = hardwareMap.get(IMU.class, "imu");

        RevHubOrientationOnRobot.LogoFacingDirection logoDirection =
                RevHubOrientationOnRobot.LogoFacingDirection.RIGHT;
        RevHubOrientationOnRobot.UsbFacingDirection usbDirection =
                RevHubOrientationOnRobot.UsbFacingDirection.BACKWARD;

        RevHubOrientationOnRobot orientationOnRobot =
                new RevHubOrientationOnRobot(logoDirection, usbDirection);

        imu.initialize(new IMU.Parameters(orientationOnRobot));
    }

    @Override
    public void loop() {

        // Reset yaw
        if (gamepad1.dpadUpWasPressed()) {
            imu.resetYaw();
        }

        driveFieldRelative(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);

        // Speed multiplier
        if (gamepad1.right_bumper) {
            speedMult = 1.5;
        } else {
            speedMult = 1;
        }

        // Intake toggle behavior (currently hold-to-run)
        intakeOn = gamepad1.b;

        if ((gamepad1.dpadLeftWasPressed()) && (flywheelSpeed > 0)) {
            flywheelSpeed -= 0.02;
        }
        if ((gamepad1.dpadRightWasPressed()) && (flywheelSpeed < 1)) {
            flywheelSpeed += 0.02;
        }


        // Lift positions
        if (gamepad1.x) {
            lift.setPosition(0.7);
        }
        if (gamepad1.y) {
            lift.setPosition(0.1);
        }

        // NEW: Linear servo control (choose buttons that do NOT conflict)
        // Default: gamepad1 left_bumper retract, gamepad1 left_trigger extend
        // (Right bumper is already used for speedMult, so avoid it)

        if ((gamepad1.leftBumperWasPressed()) && (lServoPos > 0.05)) {
            lServoPos -= 0.02;
        }
        if ((gamepad1.dpadDownWasPressed()) && (lServoPos < 0.95)) {
            lServoPos += 0.02;
        }
        linearServo.setPosition(lServoPos);

        if (intakeOn) {
            flywheel.setPower(-0.2);
            intake.setPower(0.6);
            feed1.setPosition(0.1);
            feed2.setPosition(0.9);
            pusher.setPosition(0.08);
            launchState = 0;
            launchStateTime.reset();
        } else {
            flywheel.setPower(flywheelSpeed);

            // Launch advance (A) with timing gate
            if ((gamepad1.a) && (launchStateTime.seconds() >= 2.09)) {
                launchState += 1;
                launchStateTime.reset();
            }

            if (launchState == 0) {
                pusher.setPosition(0.08);
                intake.setPower(0);
                feed1.setPosition(0.5);
                feed2.setPosition(0.5);
            }

            if (launchState == 1) {
                if (launchStateTime.seconds() < 0.5) {
                    intake.setPower(0.6);
                    feed1.setPosition(0.65);
                    feed2.setPosition(0.35);
                }
                if ((launchStateTime.seconds() >= 0.25) && (launchStateTime.seconds() < 0.5)) {
                    pusher.setPosition(0.28);
                }
                if ((launchStateTime.seconds() >= 0.7) && (launchStateTime.seconds() < 2.4)) {
                    pusher.setPosition(0.08);
                    intake.setPower(0.2);
                    feed1.setPosition(0.65);
                    feed2.setPosition(0.35);
                }
                if (launchStateTime.seconds() >= 2.4) {
                    intake.setPower(0);
                    feed1.setPosition(0.5);
                    feed2.setPosition(0.5);
                }
            }

            if (((launchState > 1) && (!gamepad1.a)) ||
                    ((launchStateTime.seconds() > 2.5) && (launchState == 1) && (!gamepad1.a))) {
                launchState = 0;
            }
        }

        telemetry.addData("Linear Servo Position", "%.2f", linearServo.getPosition());
        telemetry.addData("Flywheel Speed", "%.2f", flywheelSpeed);
        telemetry.update();
    }

    private void driveFieldRelative(double forward, double right, double rotate) {
        double theta = Math.atan2(forward, right);
        double r = Math.hypot(right, forward);

        theta = AngleUnit.normalizeRadians(theta -
                imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS));

        double newForward = r * Math.sin(theta);
        double newRight = r * Math.cos(theta);

        drive(newForward, newRight, rotate);
    }

    public void drive(double forward, double right, double rotate) {
        double frontLeftPower = (forward + right + rotate) * 0.4 * speedMult;
        double frontRightPower = (forward - right - rotate) * 0.4 * speedMult;
        double backRightPower = (forward + right - rotate) * 0.4 * speedMult;
        double backLeftPower = (forward - right + rotate) * 0.4 * speedMult;

        double maxPower = 1.0;
        maxPower = Math.max(maxPower, Math.abs(frontLeftPower));
        maxPower = Math.max(maxPower, Math.abs(frontRightPower));
        maxPower = Math.max(maxPower, Math.abs(backRightPower));
        maxPower = Math.max(maxPower, Math.abs(backLeftPower));

        frontLeftDrive.setPower(frontLeftPower / maxPower);
        frontRightDrive.setPower(frontRightPower / maxPower);
        backLeftDrive.setPower(backLeftPower / maxPower);
        backRightDrive.setPower(backRightPower / maxPower);
    }
}



