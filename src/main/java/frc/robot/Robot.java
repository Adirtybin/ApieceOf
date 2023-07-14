/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.sensors.Pigeon2;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import com.ctre.phoenix.motorcontrol.TalonFXFeedbackDevice;
import com.ctre.phoenix.motorcontrol.TalonFXInvertType;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ColorSensorV3;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.CANSparkMax.IdleMode;
import java.lang.Math;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import frc.robot.subsystems.Pneumatic;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj2.command.Command;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonUtils;
/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  ShuffleboardTab NewTab = Shuffleboard.getTab("NewTab");
  private NetworkTableEntry GyroYaw = NewTab.add("GyroYaw",0).getEntry();
  private NetworkTableEntry LimitPitch = NewTab.add("LimitPitch",false).getEntry();
  private NetworkTableEntry LimitSpin = NewTab.add("LimitSpin",false).getEntry();
  private NetworkTableEntry EncoderPitch = NewTab.add("EncoderPitch",0).getEntry();
  private NetworkTableEntry EncoderSpin = NewTab.add("EncoderSpin",0).getEntry();
  private NetworkTableEntry TargetDistance = NewTab.add("TargetDistance",0).getEntry();
  private NetworkTableEntry TargetShooterSpeed = NewTab.add("TargetShooterSpeed",0).getEntry();
  private NetworkTableEntry CurrentShooterSpeed_unitPer100ms = NewTab.add("CurrentShooterSpeed_unitPer100ms",0).getEntry();
  private NetworkTableEntry PitchAngle_Degree = NewTab.add("PitchAangle_Degree",0).getEntry();
  private NetworkTableEntry LeftWheel = NewTab.add("LeftWheel",0).getEntry();
  private NetworkTableEntry RightWheel = NewTab.add("RightWheel",0).getEntry();
  private NetworkTableEntry SetPitch = NewTab.add("SetPitch",0).getEntry();
  private NetworkTableEntry TeamSelect = NewTab.add("TeamSelect",false).getEntry();
  
  int STEP=0;

  private static final double Inv = 1;
  Timer mTimer= new Timer();
  private Command m_autonomousCommand;
  double error_spin_get = 0;
  Boolean taskf = false;
  String team = "empty";
  Pneumatic m_pneumatic = new Pneumatic();
  XboxController xbox = new XboxController(0);
  CANSparkMax motor_pitch = new CANSparkMax(16,MotorType.kBrushless);
  CANSparkMax motor_spin = new CANSparkMax(15,MotorType.kBrushless);
  RelativeEncoder encoder_pitch = motor_pitch.getEncoder();
  RelativeEncoder encoder_spin =motor_spin.getEncoder();
  CANSparkMax motor_transmit_3= new CANSparkMax(14,MotorType.kBrushless);
  CANSparkMax ball_collector= new CANSparkMax(11,MotorType.kBrushless);
  CANSparkMax ball_transmitor_1 = new CANSparkMax(12,MotorType.kBrushed);
  CANSparkMax ball_transmitor_2= new CANSparkMax(13,MotorType.kBrushed);
  CANSparkMax drive_left_1= new CANSparkMax(33,MotorType.kBrushless);
  CANSparkMax drive_left_2 = new CANSparkMax(34,MotorType.kBrushless);
  CANSparkMax drive_right_1=new CANSparkMax(32,MotorType.kBrushless);
  CANSparkMax drive_right_2= new CANSparkMax(31,MotorType.kBrushless);
  
  RelativeEncoder encoder_leftdrive= drive_left_1.getEncoder();
  RelativeEncoder encoder_rightdrive= drive_right_1.getEncoder();

  DifferentialDrive m_driver = new DifferentialDrive(drive_left_1, drive_right_2);

  DigitalInput limitsw = new DigitalInput(1);
  DigitalInput limitsw2 = new DigitalInput(2);
  DigitalInput ball_detector = new DigitalInput(0);

  String ball_1 = "empty";
  String ball_2 = "empty";

  I2C.Port i2cPort = I2C.Port.kOnboard;
  ColorSensorV3 m_colorsensor = new ColorSensorV3(i2cPort);
  
  double pitch = 0;
  double enc_rever = 0;
  double previous_error = 0;
  double Integral = 0;
  double derivative = 0;
  double output = 0;
  double setvelo = 0;
  double tarpitch = 0;
  double joypitch = 0;
  TalonFX _talon = new TalonFX(24);
  PhotonCamera frontcam = new PhotonCamera("ShooterCam");
  
  double camera_height_m = 0.685;
  double target_height_m = 2.67;
  double install_camera_pitch_r =29*Math.PI/180;
  double distance = 0;
  double Integral_spin = 0;
  double previous_error_spin = 0;
  double derivative_spin = 0;
  double error_spin = 0;
  double cam_pitch_degree = 0;
  double costatus = 1;
  double dis_error = 0.45;
  double spin_initiate_degree = -6;

  Pigeon2 gyro = new Pigeon2(3);

  double p_e = 0;

  double spin_input = 0;
	
	TalonFX _talon2 = new TalonFX(25);
  Joystick _joy = new Joystick(1);

	double targetVelocity_UnitsPer100ms = 0;
	double pre_targetvelocity_Unitsper100ms = 0;
    

	double current_velocity = 0;


	double converted_speed = 0;
  public double color_select(String team, String color){
    double color_spin = 0;
    if(team == "red"){
      if(color == "blue"){
        color_spin = 20;
      }
    }else if(team == "blue"){
      if(color == "red"){
        color_spin = 20;;
      }
    }else{
      color_spin = 0;
    }
    return color_spin;
  }
    
  public double spin_check(double dir_input, double current){
    double spin = 0;
    if(current<=-110){//-145
      if(dir_input<0){
        spin = 0;
      }else{
        spin = dir_input;
      }
    }
    else if(current>= 0){//-10
      if(dir_input>0){
        spin = 0;
      }else{
        spin = dir_input;
      }
    }
    else{
      spin = dir_input;
    }
    return spin; 
  }

  public double get_x_speed(double deg, double speedms){
    double speedx = 0;
    speedx = speedms * Math.cos(deg*Math.PI/180);
    return speedx;
  }
  public double get_y_speed(double deg, double speedms){
    double speedy = 0;
    speedy = -speedms * Math.sin(deg*Math.PI/180);
    return speedy;
  }
  
  public double spinenctodeg(double enc){
    double ev = 0;
    ev = -2 - (enc/20/10.2069*360);
    return ev;
  }

  public double enctodeg(double canencnum){
    double val = 0;
    val = 90 - (-canencnum/20/19.33*360+6) ;
  
    return val;
  }

  public double degtoenc(double degree){
    double aval = 0;

    aval = -(((90-degree) -6)/360*19.33*20);

    return aval;
  }

  public double setang(double kp, double ki, double kd, double error){     
    Integral = (Integral + error);
    derivative = (error-previous_error);
    output = ((error * kp)+(kd*derivative)+(ki*Integral));
    previous_error = error;
    
    return output;
  }

  public double get_distance(double degree){
    double distance=0;
    
    distance = PhotonUtils.calculateDistanceToTargetMeters(camera_height_m, target_height_m, install_camera_pitch_r, degree*Math.PI/180)+dis_error;

    return distance;
  }

  public double get_distance_lime(double degree){     
    double camera_angle = degree;
    double camfgle = 29 +camera_angle;
    double alttitude = target_height_m*100 - camera_height_m*100;
    double tanresul = Math.tan(camfgle*Math.PI/180);
    double dis = alttitude/tanresul;
    double distance_lime = dis + 60;
    double error = 0.3015*(Math.pow(Math.E,0.0087*distance_lime));
    double final_dis = (distance + error+23.15)/100;
    
    return final_dis;
  }


  public double cal_degree(double dism, double tarhm, double camhm){
    double ang=0;
    
    double x1 = dism;
    double y1 = tarhm - camhm;
    double x2 = dism - 0.2;
    double y2 = y1 +0.1;
    double palabala_a = ((y1*x2/x1)-y2)/((x1*x2)-(x2*x2));
    double palabala_b = (y1-(x1*x1*palabala_a))/x1;
    double vertex_x = -palabala_b/(2*palabala_a);
    double vertex_y = (-palabala_b*palabala_b)/(4*palabala_a);
    ang = (Math.atan((2*vertex_y)/vertex_x))*180/3.14;

    return ang;
  }

  public double cal_velo(double dism, double tarhm, double camhm){
    double velo = 0;
    double ang=0;
    
    double x1 = dism;
    double y1 = tarhm - camhm;
    double x2 = dism - 0.2;
    double y2 = y1 +0.1;
    double palabala_a = ((y1*x2/x1)-y2)/((x1*x2)-(x2*x2));
    double palabala_b = (y1-(x1*x1*palabala_a))/x1;
    double vertex_x = -palabala_b/(2*palabala_a);
    double vertex_y = (-palabala_b*palabala_b)/(4*palabala_a);
    ang = (Math.atan((2*vertex_y)/vertex_x))*180/3.14;
    double x = (vertex_x)*2;
    double fenzi = x*x*9.8;
    double fenmu = Math.sin(3.14/180*2*ang)*Math.cos(3.14/180*ang);
    velo = Math.pow((fenzi/fenmu),(1.0/3.0))*3-0.15;

    return velo;
  }

  public double cal_velo_move(double velocity_static, double angle_static, double yc_speed){
    double velo_move = 0;

    double yball_speed = (Math.cos(angle_static*Math.PI/180)*velocity_static)+yc_speed;
    velo_move = yball_speed/Math.cos(angle_static*Math.PI/180);

    return velo_move;
  }

  public double spin_correction(double dism, double tarhm, double camhm, double xspeed, double new_velo){
    double sc = 0;
    double time = 0;
    double cdis = 0;
    double ang=0;
    
    double x1 = dism;
    double y1 = tarhm - camhm;
    double x2 = dism - 0.2;
    double y2 = y1 +0.1;
    double palabala_a = ((y1*x2/x1)-y2)/((x1*x2)-(x2*x2));
    double palabala_b = (y1-(x1*x1*palabala_a))/x1;
    double vertex_x = -palabala_b/(2*palabala_a);
    double vertex_y = (-palabala_b*palabala_b)/(4*palabala_a);
    ang = (Math.atan((2*vertex_y)/vertex_x))*180/3.14;

    double x = (vertex_x)*2;

    time = x/(new_velo*Math.cos(ang*Math.PI/180));
    cdis = time*xspeed;
    sc = Math.atan(cdis/dism)*180/Math.PI;

    return sc;
  } 

  public double auto_turn(double current_angle, double target_angle){
    target_angle = -target_angle;
    double auto_turn_output = 0;
    double atkp = 0.004;
    double atki = 0.00001;

    auto_turn_output = (target_angle - current_angle)*atkp + (target_angle - current_angle)*atki;
    p_e = (target_angle - current_angle)+p_e;

    return auto_turn_output;
  }

  public double auto_drivestraight(double current_encoder,double target_meter){
    double auto_straight_out = 0;
    double askp = 0.02;

    auto_straight_out = ((target_meter*22.428)+current_encoder)*askp;

    return auto_straight_out;
  }

  public double falcon500_delay(double targetvelo, double actualvelo){
    if((targetvelo-actualvelo)>=1000){
      setvelo = setvelo + 75;
    }else if((targetvelo-actualvelo)<=-1000){
      setvelo = setvelo - 75;
    }else if(targetvelo == 0){
      if(Math.abs(0-actualvelo)<600){
        setvelo = actualvelo;
      }else{
        setvelo = 0;
      }
    }else{
      setvelo = targetvelo;
    }
  
    // SmartDashboard.putNumber("TargetShooterSpeed", setvelo);
    // TargetShooterSpeed.setDouble(setvelo);
    return setvelo;
  }

  public void launcher_set(double setv){
    targetVelocity_UnitsPer100ms = -setv;
    if (setv == 0){
      _talon.set(TalonFXControlMode.Velocity, -falcon500_delay(0, -_talon.getSelectedSensorVelocity()));
    }else{
      /* 500 RPM in either direction */
      _talon.set(TalonFXControlMode.Velocity, -falcon500_delay(-targetVelocity_UnitsPer100ms, -_talon.getSelectedSensorVelocity()));
    }
    current_velocity = -_talon.getSelectedSensorVelocity();
    converted_speed = current_velocity/2048*10*1.5*10.16*Math.PI/100;
    pre_targetvelocity_Unitsper100ms = targetVelocity_UnitsPer100ms;
    if ((-targetVelocity_UnitsPer100ms)<0.001){
      targetVelocity_UnitsPer100ms = 0;
    }
  }

  public double get_drive_speed(){
    double speed_d = encoder_leftdrive.getVelocity()/10.71*0.152*Math.PI/60;

    return speed_d;
  }

  public void go_straight(double meters,double enc){
    drive_left_1.set(auto_drivestraight(-enc, -meters));
    drive_right_2.set(auto_drivestraight(-enc, -meters));
  }

  public void auto_shoot(){
    motor_spin.set(spin_check(spin_input, encoder_spin.getPosition()));

    double kp_spin = 0.017;
    double ki_spin = 0.00000;
    SmartDashboard.putNumber("HAST", error_spin);
    var result = frontcam.getLatestResult();
    SmartDashboard.putBoolean("HASSSS",result.hasTargets());
    if(result.hasTargets()){
      error_spin = result.getBestTarget().getYaw();
      cam_pitch_degree = result.getBestTarget().getPitch();
      
      Integral_spin = (Integral_spin + error_spin);

      spin_input = (error_spin * kp_spin)+(ki_spin*Integral);
      previous_error_spin = error_spin;

      distance = get_distance(cam_pitch_degree);
    
      tarpitch = cal_degree(distance, target_height_m, camera_height_m);
      if (tarpitch<45){
        motor_pitch.set(0);
      }else if(tarpitch>80){
        motor_pitch.set(0);
      }else{
        motor_pitch.set(setang(0.01, 0.000015, 0, degtoenc(tarpitch)-encoder_pitch.getPosition()));
      }
      PitchAngle_Degree.setDouble(spinenctodeg(encoder_pitch.getPosition()));
      SetPitch.setDouble(tarpitch);

      double vertedcon_velo = cal_velo(distance, target_height_m, camera_height_m)*100/Math.PI/10.16/1.5/10*2048;
      launcher_set(vertedcon_velo);

    }else{
      spin_input = -0.2;
    }
  }

  public Boolean auto_detect(){
    Boolean autoshoot = false;
    
    if((degtoenc(tarpitch)-encoder_pitch.getPosition())<3){
      if(Math.abs(-targetVelocity_UnitsPer100ms) >= Math.abs(-_talon.getSelectedSensorVelocity())){
        autoshoot = true;
      }
    }
    return autoshoot;
  }

  public String get_color(){
    /*
    Color kBlueTarget = new Color(0.15, 0.4, 0.44);
    Color kRedTarget = new Color(0.561, 0.232, 0.114);
    Color kEmptyTarget = new Color(0.25, 0.49, 0.26);
    */
    String co = "";
    Color detectedColor = m_colorsensor.getColor();
    double R = detectedColor.red;
    double G = detectedColor.green;
    double B = detectedColor.blue;

    if ((Math.abs(0.15-R)<0.1)&&(Math.abs(0.4-G)<0.1)&&(Math.abs(0.44-B)<0.1)){
      co = "blue";
    }else if ((Math.abs(0.53-R)<0.2)&&(Math.abs(0.34-G)<0.2)&&(Math.abs(0.118-B)<0.2)){
      co = "red";
    }else if ((Math.abs(0.25-R)<0.1)&&(Math.abs(0.49-G)<0.1)&&(Math.abs(0.26-B)<0.1)){
      co = "empty";
    }else{
      co = "empty";
    }
    return co;
  }

  @Override
  public void robotInit() {
    motor_pitch.setInverted(true);
    ball_collector.setInverted(true);
    drive_left_1.setInverted(true);

    drive_left_1.setIdleMode(IdleMode.kBrake);
    drive_left_2.setIdleMode(IdleMode.kBrake);
    drive_right_1.setIdleMode(IdleMode.kBrake);
    drive_right_2.setIdleMode(IdleMode.kBrake);

    drive_left_2.follow(drive_left_1);
    drive_right_1.follow(drive_right_2);

    /* Factory Default all hardware to prevent unexpected behaviour */
		_talon.configFactoryDefault();
		_talon2.configFactoryDefault();
		
		/* Config neutral deadband to be the smallest possible */
		_talon.configNeutralDeadband(0.001);
		_talon2.configNeutralDeadband(0.001);

    /* Config motor inverted */
		_talon.setInverted(TalonFXInvertType.Clockwise); // !< Update this
    _talon2.setInverted(TalonFXInvertType.CounterClockwise); // !< Update this

		/* configuring talon2*/
		_talon2.follow(_talon);

		/* Config sensor used for Primary PID [Velocity] */
    _talon.configSelectedFeedbackSensor(TalonFXFeedbackDevice.IntegratedSensor,
                                        Constants.kPIDLoopIdx, 
											                  Constants.kTimeoutMs);
											
		/* Config the peak and nominal outputs */
		_talon.configNominalOutputForward(0, Constants.kTimeoutMs);
		_talon.configNominalOutputReverse(0, Constants.kTimeoutMs);
		_talon.configPeakOutputForward(1, Constants.kTimeoutMs);
		_talon.configPeakOutputReverse(-1, Constants.kTimeoutMs);

		/* Config the Velocity closed loop gains in slot0 */
		_talon.config_kF(Constants.kPIDLoopIdx, Constants.kGains_Velocit.kF, Constants.kTimeoutMs);
		_talon.config_kP(Constants.kPIDLoopIdx, Constants.kGains_Velocit.kP, Constants.kTimeoutMs);
		_talon.config_kI(Constants.kPIDLoopIdx, Constants.kGains_Velocit.kI, Constants.kTimeoutMs);
		_talon.config_kD(Constants.kPIDLoopIdx, Constants.kGains_Velocit.kD, Constants.kTimeoutMs);
		/*
		 * Talon FX does not need sensor phase set for its integrated sensor
		 * This is because it will always be correct if the selected feedback device is integrated sensor (default value)
		 * and the user calls getSelectedSensor* to get the sensor's position/velocity.
		 * 
		 * https://phoenix-documentation.readthedocs.io/en/latest/ch14_MCSensor.html#sensor-phase
		 */
    // _talon.setSensorPhase(true);
		//_talon2.follow(_talon);      
    // Instantiate our RobotContainer.  This will perform all our button bindings, and put our
    // autonomous chooser on the dashboard.

  }
  
  @Override
  public void robotPeriodic() {
    // Runs the Scheduler.  This is responsible for polling buttons, adding newly-scheduled
    // commands, running already-scheduled commands, removing finished or interrupted commands,
    // and running subsystem periodic() methods.  This must be called from the robot's periodic
    // block in order for anything in the Command-based framework to work.
    
    LeftWheel.setDouble(encoder_leftdrive.getPosition());
    RightWheel.setDouble(encoder_rightdrive.getPosition());
    LimitPitch.setBoolean(limitsw.get());
    LimitSpin.setBoolean(limitsw2.get());
    EncoderPitch.setDouble(encoder_pitch.getPosition());
    EncoderSpin.setDouble(encoder_spin.getPosition());
    TargetDistance.setDouble(distance);
    GyroYaw.setDouble(gyro.getYaw());
    CurrentShooterSpeed_unitPer100ms.setDouble(current_velocity);
    TargetShooterSpeed.setDouble(setvelo);
    SmartDashboard.putNumber("intakeMotorTemp", ball_collector.getOutputCurrent());
 }

  /**
   * This function is called once each time the robot enters Disabled mode.
   */
  @Override
  public void disabledInit() {
    
  }

  @Override
  public void disabledPeriodic() {
  }

  /**
   * This autonomous runs the autonomous command selected by your {@link RobotContainer} class.
   */
  @Override
  public void autonomousInit() {
    // schedule the autonomous command (example)
    
    if (m_autonomousCommand != null) {
      m_autonomousCommand.schedule();
    }

    _talon.set(TalonFXControlMode.PercentOutput, 0);
    mTimer.start();
    mTimer.reset();
    while(mTimer.get()<2){
      if (!limitsw.get()){
        motor_pitch.set(0);
        break;
      }else{
        motor_pitch.set(0.25);  
      }
    }

    encoder_pitch.setPosition(0);

    mTimer.reset();
    while (mTimer.get()<2){
      if(limitsw2.get()){
        motor_spin.set(0);
        break;
      }else{
        motor_spin.set(0.25);
      }
    }

    encoder_spin.setPosition(0);
    encoder_leftdrive.setPosition(0);
    encoder_rightdrive.setPosition(0);
    gyro.setYaw(0);
    _talon.set(TalonFXControlMode.PercentOutput, 0);
    ball_collector.set(0); 

    _talon.set(TalonFXControlMode.PercentOutput, 0);

    mTimer.start();
    mTimer.reset();
    while(mTimer.get()<2){
      if (!limitsw.get()){
        motor_pitch.set(0);
        break;
      }else{
        motor_pitch.set(0.25);  
      }
    }

    encoder_pitch.setPosition(0);

    mTimer.reset();
    while (mTimer.get()<2){
      if(limitsw2.get()){
        motor_spin.set(0);
        break;
      }else{
        motor_spin.set(0.25);
      }
    }

    encoder_spin.setPosition(0);
    encoder_leftdrive.setPosition(0);
    encoder_rightdrive.setPosition(0);
    gyro.setYaw(0);
    _talon.set(TalonFXControlMode.PercentOutput, 0);
    ball_collector.set(0); 



    //This is shit
/************************************************* */
    mTimer.reset();
    mTimer.start();
    STEP = 1;
  }
  
  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    SmartDashboard.putNumber("step", STEP);
    switch(STEP){
      case 0:
          m_pneumatic.intakeDown();
          ball_collector.set(0.5);
          if(mTimer.get() > 0.2){
            STEP=1;
          }
          break;
      case 1:
          double drive_current_drivestraight = encoder_leftdrive.getPosition();
          go_straight(1.2, drive_current_drivestraight);
          if(Math.abs(21.65*1.2-Math.abs(encoder_leftdrive.getPosition()))<5)
          {STEP=2;}
          break;
      case 2:
          m_driver.arcadeDrive(0, 0);
          auto_spin();
          auto_pitch();
          auto_shooter();
          kaipao();
          break;
      default:
          break;
    }
  }

  @Override
  public void teleopInit() {
    if (m_autonomousCommand != null) {
      m_autonomousCommand.cancel();
    }
    launcher_set(0);
    while(true){
      if (!limitsw.get()){
        motor_pitch.set(0);
        
        break;
      }else{
        motor_pitch.set(0.25);
      }
    }
    encoder_pitch.setPosition(0);

    while (true){
      if(limitsw2.get()){
        // System.out.println("TEST");
        motor_spin.set(0);
        break;
      }else{
        
        motor_spin.set(0.25);
      }
    }
    encoder_spin.setPosition(0);
    
    gyro.setYaw(0);
    m_pneumatic.intakeUp();
    
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    if (!limitsw.get()){
      encoder_pitch.setPosition(0);
    }
    if(limitsw2.get()){
      encoder_spin.setPosition(0);
    }
    if(_joy.getRawAxis(3)<0){
      team = "blue";
      TeamSelect.setBoolean(true);
    }else{
      team = "red";
      TeamSelect.setBoolean(false);
    }
    
    if(ball_1=="empty"){
      ball_1 = get_color();
    }else if(!(ball_1 == get_color())){
      ball_2 = get_color();
    }
    if(!ball_detector.get()){
      ball_1 = ball_2;
      
    }

    SmartDashboard.putString("color1",ball_1);
    SmartDashboard.putNumber("xspeed", get_x_speed(spinenctodeg(encoder_spin.getPosition()), get_drive_speed()));
    SmartDashboard.putNumber("yspeed", get_y_speed(spinenctodeg(encoder_spin.getPosition()), get_drive_speed()));
    SmartDashboard.putNumber("LeftMotor1Temp", drive_left_1.getMotorTemperature());
    SmartDashboard.putNumber("LeftMotor2Temp", drive_left_2.getMotorTemperature());
    SmartDashboard.putBoolean("ats", auto_detect());
    
    double straight_velo = xbox.getRawAxis(1)*0.8;
    double turn_velo = -xbox.getRawAxis(4)*0.8;
    m_driver.arcadeDrive(straight_velo, turn_velo);

    motor_spin.set(spin_check(spin_input, encoder_spin.getPosition()));
    SmartDashboard.putNumber("SpinTurned", spin_check(spin_input, encoder_spin.getPosition()));
    SmartDashboard.putNumber("rorrspin", error_spin);
    
    if(xbox.getRawButton(2)){
      m_pneumatic.intakeUp();
    }if(xbox.getRawButton(3)){
      m_pneumatic.intakeDown();
    }
    
    if(_joy.getPOV()==270){
      spin_input = -0.3;
    
    }else if(_joy.getPOV()==90){
      spin_input = 0.3;
    }else{
      spin_input = 0;
    }
    
    if(_joy.getRawButton(1)){
      ball_transmitor_2.set(0.3*-Inv);
      motor_transmit_3.set(-0.75);
      
    }else{
      ball_transmitor_2.set(0);
      motor_transmit_3.set(-0);
    }

    if(_joy.getPOV()==180){
      motor_pitch.set(0.2);
    }else if(_joy.getPOV()==0){
      motor_pitch.set(-0.2);
    }else{
      motor_pitch.set(0);
    }

    if(xbox.getRawAxis(3)>=0.1){ 
      ball_collector.set(xbox.getRawAxis(3));
      SmartDashboard.putNumber("collectorMotorPower", xbox.getRawAxis(3));
    }else if(xbox.getRawAxis(2)>=0.1){
      ball_collector.set(-xbox.getRawAxis(2));
    }else{
      ball_collector.set(0);
      m_pneumatic.intakeUp();
    }
    
    if (xbox.getRawButton(5)){
      m_pneumatic.intakeDown();
    }else if(xbox.getRawButton(6)){ 
      m_pneumatic.intakeUp();
    }

    if(xbox.getRawAxis(3)>=0.1){
      ball_transmitor_1.set(-0.8*Inv);
      m_pneumatic.intakeDown();
    }else if(_joy.getRawButton(1)){
      ball_transmitor_1.set(-0.8*Inv);
    }else if(xbox.getRawAxis(2)>=0.1){
      ball_transmitor_1.set(0.8*Inv);
    }else{
      ball_transmitor_1.set(0);
    }
    
    if (_joy.getRawButton(2)){
      double kp_spin = 0.02;
      double ki_spin = 0.0000;
 
      var result = frontcam.getLatestResult();
      if(result.hasTargets()){
        error_spin_get = result.getBestTarget().getYaw();
        cam_pitch_degree = result.getBestTarget().getPitch();

      }else{
        
      }

      if (Math.abs(spin_correction(distance, target_height_m, camera_height_m, get_x_speed(spinenctodeg(encoder_spin.getPosition()), get_drive_speed()),cal_velo_move(cal_velo(distance, target_height_m, camera_height_m),tarpitch,-get_y_speed(spinenctodeg(encoder_spin.getPosition()), get_drive_speed())))+color_select(team, ball_1))<30){
        error_spin = error_spin_get + -spin_correction(distance, target_height_m, camera_height_m, get_x_speed(spinenctodeg(encoder_spin.getPosition()), get_drive_speed()),cal_velo_move(cal_velo(distance, target_height_m, camera_height_m),tarpitch,-get_y_speed(spinenctodeg(encoder_spin.getPosition()), get_drive_speed())))+color_select(team, ball_1);
      }else{
        error_spin = error_spin_get+color_select(team, ball_1);
      }

      Integral_spin = (Integral_spin + error_spin);
      spin_input = (error_spin * kp_spin)+(ki_spin*Integral);
      distance = get_distance(cam_pitch_degree);
      tarpitch = cal_degree(distance, target_height_m, camera_height_m);
    
      if (tarpitch<45){
        motor_pitch.set(0);
      }else if(tarpitch>80){
        motor_pitch.set(0);
      }else{
        motor_pitch.set(setang(0.01, 0.000015, 0, degtoenc(tarpitch)-encoder_pitch.getPosition()));
      }
    
      double vertedcon_velo = cal_velo_move(cal_velo(distance, target_height_m, camera_height_m),tarpitch,-get_y_speed(spinenctodeg(encoder_spin.getPosition()), get_drive_speed()))*100/Math.PI/10.16/1.5/10*2048;
      launcher_set(vertedcon_velo);

    }else if(_joy.getRawButton(11)){
      _talon.set(TalonFXControlMode.PercentOutput,_joy.getRawAxis(1));
    }
    else{
      _talon.set(TalonFXControlMode.PercentOutput, -0);   
    }  
  }

  @Override
  public void testInit() {
    chuansongzu(0);
    while(true){
      if (!limitsw.get()){
        motor_pitch.set(0);
        
        break;
      }else{
        motor_pitch.set(0.25);
      }
    }
    encoder_pitch.setPosition(0);

    while (true){
      if(limitsw2.get()){
        motor_spin.set(0);
        break;
      }else{
        
        motor_spin.set(0.25);
      }
    }
    encoder_spin.setPosition(0);
    encoder_leftdrive.setPosition(0); 
  }
  public void auto_shoot2(){
    double kp_spin = 0.017;
    double ki_spin = 0.00000;
    SmartDashboard.putNumber("HAST", error_spin);
    var result = frontcam.getLatestResult();
    SmartDashboard.putBoolean("HASSSS",result.hasTargets());
    if(result.hasTargets()){
      error_spin = result.getBestTarget().getYaw();
      cam_pitch_degree = result.getBestTarget().getPitch();
      Integral_spin = (Integral_spin + error_spin);
      spin_input = (error_spin * kp_spin)+(ki_spin*Integral);
      previous_error_spin = error_spin;
      distance = get_distance(cam_pitch_degree);
      tarpitch = cal_degree(distance, target_height_m, camera_height_m);
      if (tarpitch<45){
        motor_pitch.set(0);
      }else if(tarpitch>80){
        motor_pitch.set(0);
      }else{
        motor_pitch.set(setang(0.01, 0.000015, 0, degtoenc(tarpitch)-encoder_pitch.getPosition()));
      }
      double vertedcon_velo = cal_velo(distance, target_height_m, camera_height_m)*100/Math.PI/10.16/1.5/10*2048;
      launcher_set(vertedcon_velo);
    }else{
      spin_input = -0.2;
    }
  }
  
  public void auto_spin(){
    double kp_spin = 0.017;
    double ki_spin = 0.00000;
    double error_spin = 0;
    double spin_input = 0;
    double Integral_spin = 0;
    double Integral = 0;
    double previous_error_spin = 0;
    var result = frontcam.getLatestResult();
    if(result.hasTargets()){
      error_spin = result.getBestTarget().getYaw();
      cam_pitch_degree = result.getBestTarget().getPitch();
      Integral_spin = (Integral_spin + error_spin);
      spin_input = (error_spin * kp_spin)+(ki_spin*Integral);
      previous_error_spin = error_spin;
      motor_spin.set(spin_input);
    }else{
      motor_spin.set(-0.2);
    }
    
  }

  public void auto_pitch(){
    var result = frontcam.getLatestResult();
    if(result.hasTargets()){
      cam_pitch_degree = result.getBestTarget().getPitch();
      distance = get_distance(cam_pitch_degree);
      tarpitch = cal_degree(distance, target_height_m, camera_height_m);
      if (tarpitch<45){
        motor_pitch.set(0);
      }else if(tarpitch>80){
        motor_pitch.set(0);
      }else{
        motor_pitch.set(setang(0.01, 0.000015, 0, degtoenc(tarpitch)-encoder_pitch.getPosition()));
      }
    }
  }

  public void  auto_shooter(){
    var result = frontcam.getLatestResult();
    SmartDashboard.putBoolean("HASSSS",result.hasTargets());
    if(result.hasTargets()){
      error_spin = result.getBestTarget().getYaw();
      cam_pitch_degree = result.getBestTarget().getPitch();
      distance = get_distance(cam_pitch_degree);
      tarpitch = cal_degree(distance, target_height_m, camera_height_m);
      PitchAngle_Degree.setDouble(spinenctodeg(encoder_pitch.getPosition()));
      double vertedcon_velo = cal_velo(distance, target_height_m, camera_height_m)*100/Math.PI/10.16/1.5/10*2048;
      launcher_set(vertedcon_velo);
      SmartDashboard.putNumber("SHOOTER_V", vertedcon_velo);
    }else{
      launcher_set(2000);
    }
  }
  public void chuansongzu(double sudu){
    ball_transmitor_1.set(-sudu*Inv);
    ball_transmitor_2.set(-sudu*Inv);
    motor_transmit_3.set(-sudu); 
  }
  public void kaipao(){
    if (frontcam.getLatestResult().hasTargets()){
      if((degtoenc(tarpitch)-encoder_pitch.getPosition())<5){
        if(Math.abs(-targetVelocity_UnitsPer100ms) >= Math.abs(-_talon.getSelectedSensorVelocity())){
          chuansongzu(0.8);
        }else{
          
        }
      }else{

      }
    }
    SmartDashboard.putBoolean("hasTarget", frontcam.getLatestResult().hasTargets());
    SmartDashboard.putBoolean("pitch?",(degtoenc(tarpitch)-encoder_pitch.getPosition())<5);
    SmartDashboard.putNumber("degTarPitch", degtoenc(tarpitch));
    SmartDashboard.putNumber("sensorPitch", encoder_pitch.getPosition());
    SmartDashboard.putBoolean("velo???", Math.abs(-targetVelocity_UnitsPer100ms) <= Math.abs(-_talon.getSelectedSensorVelocity()));
    SmartDashboard.putNumber("tarVelo", Math.abs(-targetVelocity_UnitsPer100ms));
    SmartDashboard.putNumber("sensorVelo", Math.abs(-_talon.getSelectedSensorVelocity()));
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
    
  }
}