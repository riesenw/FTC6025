/* Copyright (c) 2025 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

/*
 * This OpMode illustrates how to program your robot to drive field relative.  This means
 * that the robot drives the direction you push the joystick regardless of the current orientation
 * of the robot.
 *
 * This OpMode assumes that you have four mecanum wheels each on its own motor named:
 *   front_left_motor, front_right_motor, back_left_motor, back_right_motor
 *
 *   and that the left motors are flipped such that when they turn clockwise the wheel moves backwards
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list
 *
 */
@TeleOp
public class MecDriveWithIntake extends OpMode {
    // This declares the four motors needed
    DcMotor frontLeftDrive;
    DcMotor frontRightDrive;
    DcMotor backLeftDrive;
    DcMotor backRightDrive;
    DcMotor flywheel;
    DcMotor intake;
    Servo feed1;
    Servo feed2;
    Servo pusher;
    Servo lift;
    Servo ramp;


    // This declares the IMU needed to get the current direction the robot is facing
    IMU imu;

    boolean intakeOn = false;

    int launchState = 0;

    double speedMult = 1;

    double launchSpeed = 0.54;

    double rampPos = 0.05;

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
        ramp = hardwareMap.get(Servo.class, "ramp");


        // We set the left motors in reverse which is needed for drive trains where the left
        // motors are opposite to the right ones.
        backLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);

        // This uses RUN_USING_ENCODER to be more accurate.   If you don't have the encoder
        // wires, you should remove these
        frontLeftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backLeftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        flywheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        imu = hardwareMap.get(IMU.class, "imu");
        // This needs to be changed to match the orientation on your robot
        RevHubOrientationOnRobot.LogoFacingDirection logoDirection =
                RevHubOrientationOnRobot.LogoFacingDirection.RIGHT;
        RevHubOrientationOnRobot.UsbFacingDirection usbDirection =
                RevHubOrientationOnRobot.UsbFacingDirection.BACKWARD;

        RevHubOrientationOnRobot orientationOnRobot = new
                RevHubOrientationOnRobot(logoDirection, usbDirection);
        imu.initialize(new IMU.Parameters(orientationOnRobot));


    }

    @Override
    public void loop() {

        // If you press the A button, then you reset the Yaw to be zero from the way
        // the robot is currently pointing
        if (gamepad1.dpadUpWasPressed()) {
            imu.resetYaw();
        }

        driveFieldRelative(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);

        if (gamepad1.right_bumper) {
            speedMult = 1.5;
        }
        else {
            speedMult = 1;
        }

        if (gamepad1.b) {
            intakeOn = true;
        }
        else {
            intakeOn = false;
        }

        if (gamepad1.x) {
            lift.setPosition(0.7);
        }

        if (gamepad1.y) {
            lift.setPosition(0.1);
        }

        if (gamepad1.leftBumperWasPressed()) {
            rampPos = 0.27;
            launchSpeed = 0.5;
        }

        if (gamepad1.left_trigger > 0.5) {
            rampPos = 0.05;
            launchSpeed = 0.54;
        }

        if (gamepad1.dpadDownWasPressed()) {
            rampPos = 0.29;
            launchSpeed = 0.24;
        }

        ramp.setPosition(rampPos);

        if (intakeOn) {
            flywheel.setPower(-0.2);
            intake.setPower(0.6);
            feed1.setPosition(0.1);
            feed2.setPosition(0.9);
            pusher.setPosition(0.08);
            launchState = 0;
            launchStateTime.reset();
        }
        else {
            flywheel.setPower(launchSpeed);
            if ((gamepad1.a) && (launchStateTime.seconds() >= 1.5)) {
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
                if (launchStateTime.seconds() < 0.43) {
                    intake.setPower(0.6);
                    feed1.setPosition(0.65);
                    feed2.setPosition(0.35);
                }
                if ((launchStateTime.seconds() >= 0.1) && (launchStateTime.seconds() < 0.35)) {
                    pusher.setPosition(0.28);
                }
                if ((launchStateTime.seconds() >= 0.5) && (launchStateTime.seconds() < 0.9)){
                    pusher.setPosition(0.08);
                }
                if ((launchStateTime.seconds() >= 1.2)){
                    intake.setPower(0);
                    feed1.setPosition(0.5);
                    feed2.setPosition(0.5);
                }
            }

            if (((launchState > 1) && (!gamepad1.a)) || ((launchStateTime.seconds() > 1) && (launchState == 1) && (!gamepad1.a))) {
                launchState = 0;
            }

        }




    }

    // This routine drives the robot field relative
    private void driveFieldRelative(double forward, double right, double rotate) {
        // First, convert direction being asked to drive to polar coordinates
        double theta = Math.atan2(forward, right);
        double r = Math.hypot(right, forward);

        // Second, rotate angle by the angle the robot is pointing
        theta = AngleUnit.normalizeRadians(theta -
                imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS));

        // Third, convert back to cartesian
        double newForward = r * Math.sin(theta);
        double newRight = r * Math.cos(theta);

        // Finally, call the drive method with robot relative forward and right amounts
        drive(newForward, newRight, rotate);
    }

    // Thanks to FTC16072 for sharing this code!!
    public void drive(double forward, double right, double rotate) {
        // This calculates the power needed for each wheel based on the amount of forward,
        // strafe right, and rotate
        double frontLeftPower = (forward + right + rotate) * 0.4 * speedMult;
        double frontRightPower = (forward - right - rotate) * 0.4 * speedMult;
        double backRightPower = (forward + right - rotate) * 0.4 * speedMult;
        double backLeftPower = (forward - right + rotate) * 0.4 * speedMult;

        double maxPower = 1.0;
        double maxSpeed = 1.0;  // make this slower for outreaches

        // This is needed to make sure we don't pass > 1.0 to any wheel
        // It allows us to keep all of the motors in proportion to what they should
        // be and not get clipped
        maxPower = Math.max(maxPower, Math.abs(frontLeftPower));
        maxPower = Math.max(maxPower, Math.abs(frontRightPower));
        maxPower = Math.max(maxPower, Math.abs(backRightPower));
        maxPower = Math.max(maxPower, Math.abs(backLeftPower));

        // We multiply by maxSpeed so that it can be set lower for outreaches
        // When a young child is driving the robot, we may not want to allow full
        // speed.
        frontLeftDrive.setPower(maxSpeed * (frontLeftPower / maxPower));
        frontRightDrive.setPower(maxSpeed * (frontRightPower / maxPower));
        backLeftDrive.setPower(maxSpeed * (backLeftPower / maxPower));
        backRightDrive.setPower(maxSpeed * (backRightPower / maxPower));
    }
}
