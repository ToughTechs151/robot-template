// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.sim;

import edu.wpi.first.hal.SimDouble;
import edu.wpi.first.wpilibj.simulation.SimDeviceSim;

/**
 * Wrapper for a simulation interface to a CANSparkMax motor controller. Provides methods to set
 * simulated values in the controller to use in place of real values during simulation.
 */
public class CANSparkMaxSim {

  // Methods to set motor controller variables
  private SimDouble simPosition;
  private SimDouble simVelocity;
  private SimDouble simMotorCurrent;

  /** Simulated CANSparkMax. */
  public CANSparkMaxSim(int motorPort) {

    // Setup an interface to the CANSparkMax and methods to set values during simulation
    SimDeviceSim sparkSim = new SimDeviceSim("SPARK MAX [" + motorPort + "]");
    simPosition = sparkSim.getDouble("Position");
    simVelocity = sparkSim.getDouble("Velocity");
    simMotorCurrent = sparkSim.getDouble("Motor Current");
  }

  public void setPosition(double position) {
    simPosition.set(position);
  }

  public void setVelocity(double velocity) {
    simVelocity.set(velocity);
  }

  public void setCurrent(double current) {
    simMotorCurrent.set(current);
  }
}
