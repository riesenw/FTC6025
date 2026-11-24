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
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
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
@Autonomous
public class MecAutoRedWithIntakeFast extends LinearOpMode {
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
    Servo ramp;

    private ElapsedTime runtime = new ElapsedTime();

    // This declares the IMU needed to get the current direction the robot is facing
    IMU imu;

    boolean flywheelOn = false;

    @Override
    public void runOpMode() {

        // Initialize the drive system variables.
        frontLeftDrive = hardwareMap.get(DcMotor.class, "lf");
        frontRightDrive = hardwareMap.get(DcMotor.class, "rf");
        backLeftDrive = hardwareMap.get(DcMotor.class, "lr");
        backRightDrive = hardwareMap.get(DcMotor.class, "rr");
        flywheel = hardwareMap.get(DcMotor.class, "flywheel");
        intake = hardwareMap.get(DcMotor.class, "intake");

        feed1 = hardwareMap.get(Servo.class, "feed 1");
        feed2 = hardwareMap.get(Servo.class, "feed 2");
        pusher = hardwareMap.get(Servo.class, "pusher");
        ramp = hardwareMap.get(Servo.class, "ramp");

        // To drive forward, most robots need the motor on one side to be reversed, because the axles point in opposite directions.
        // When run, this OpMode should start both motors driving forward. So adjust these two lines based on your first test drive.
        // Note: The settings here assume direct drive on left and right wheels.  Gear Reduction or 90 Deg drives may require direction flips
        backLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);

        frontLeftDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRightDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backLeftDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backRightDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        // This uses RUN_USING_ENCODER to be more accurate.   If you don't have the encoder
        // wires, you should remove these
        frontLeftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backLeftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        flywheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        frontLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        // Send telemetry message to indicate successful Encoder reset

        imu = hardwareMap.get(IMU.class, "imu");
        // This needs to be changed to match the orientation on your robot
        RevHubOrientationOnRobot.LogoFacingDirection logoDirection =
                RevHubOrientationOnRobot.LogoFacingDirection.RIGHT;
        RevHubOrientationOnRobot.UsbFacingDirection usbDirection =
                RevHubOrientationOnRobot.UsbFacingDirection.BACKWARD;

        RevHubOrientationOnRobot orientationOnRobot = new
                RevHubOrientationOnRobot(logoDirection, usbDirection);
        imu.initialize(new IMU.Parameters(orientationOnRobot));

        // Wait for the game to start (driver presses START)
        waitForStart();

        // Step through each leg of the path,
        // Note: Reverse movement is obtained by setting a negative distance (not speed)


        imu.resetYaw();
        pusher.setPosition(0.08);
        flywheel.setPower(0.48);
        ramp.setPosition(0.20);

        drive(-1,0,0);

        sleep(770);
        //start flywheel
        drive(0,0,0);

        sleep(4730);

        feed1.setPosition(0.7);
        feed2.setPosition(0.3);
        intake.setPower(0.4);

        sleep(400);

        feed1.setPosition(0.5);
        feed2.setPosition(0.5);
        intake.setPower(-0.2);

        sleep(200);


        feed1.setPosition(0.7);
        feed2.setPosition(0.3);
        intake.setPower(0.4);

        sleep(800);

        pusher.setPosition(0.28);

        sleep(400);

        pusher.setPosition(0.08);

        sleep(400);

        intake.setPower(-0.2);
        feed1.setPosition(0.5);
        feed2.setPosition(0.5);

        sleep(400);

        intake.setPower(0);

        sleep(500);

        feed1.setPosition(0.7);
        feed2.setPosition(0.3);
        intake.setPower(0.3);

        sleep(400);

        pusher.setPosition(0.28);

        sleep(400);

        pusher.setPosition(0.08);

        sleep(400);

        feed1.setPosition(0.5);
        feed2.setPosition(0.5);
        intake.setPower(0);

        drive(-1,0,0);
        sleep(2000);
        drive(0,0,1);
        sleep(360);
        drive(1,0,0);
        sleep(1000);
        flywheel.setPower(-0.2);
        intake.setPower(0.6);
        feed1.setPosition(0.1);
        feed2.setPosition(0.9);
        drive(0.3, 0, 0);
        sleep(2500);
        drive(0,0,0);
        sleep(2000);
        pusher.setPosition(0.08);
        intake.setPower(0);
        feed1.setPosition(0.5);
        feed2.setPosition(0.5);
        flywheel.setPower(0.48);
        sleep(500);
        driveFieldRelative(0, 0, -1);
        sleep(480);
        driveFieldRelative(1, -1, 0);
        sleep(1300);
        driveFieldRelative(0, 0, 0);
        sleep(2850);

        intake.setPower(0.5);
        feed1.setPosition(0.7);
        feed2.setPosition(0.3);

        sleep(200);

        pusher.setPosition(0.28);

        sleep(400);

        pusher.setPosition(0.08);

        sleep(1000);

        intake.setPower(0);
        feed1.setPosition(0.5);
        feed2.setPosition(0.5);

        sleep(500);

        intake.setPower(0.5);
        feed1.setPosition(0.7);
        feed2.setPosition(0.3);

        sleep(200);

        pusher.setPosition(0.28);

        sleep(400);

        pusher.setPosition(0.08);

        sleep(300);

        intake.setPower(-0.2);
        feed1.setPosition(0.5);
        feed2.setPosition(0.5);

        sleep(200);

        intake.setPower(0.5);
        feed1.setPosition(0.7);
        feed2.setPosition(0.3);

        sleep(200);

        pusher.setPosition(0.28);

        sleep(400);

        pusher.setPosition(0.08);

        sleep(500);

        intake.setPower(0);
        feed1.setPosition(0.5);
        feed2.setPosition(0.5);
        drive(-1.5,2,0);

        sleep(1000);


        drive(0, 0, 0);
    }
    // This routine drives the robot field relative
    private void driveFieldRelative ( double forward, double right, double rotate){
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
    public void drive ( double forward, double right, double rotate){
        // This calculates the power needed for each wheel based on the amount of forward,
        // strafe right, and rotate
        double frontLeftPower = (forward + right + rotate) * 0.4;
        double frontRightPower = (forward - right - rotate) * 0.4;
        double backRightPower = (forward + right - rotate) * 0.4;
        double backLeftPower = (forward - right + rotate) * 0.4;

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
