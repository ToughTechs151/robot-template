package frc.robot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import edu.wpi.first.hal.AllianceStationID;
import edu.wpi.first.wpilibj.PWM;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import frc.robot.subsystems.BlinkinSubsystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BlinkinSubsystemTest {

  private boolean mainState = false;
  private double whatDouble = 0.0;
  private PWM pwm;

  private boolean isMainTrue() {
    return mainState;
  }

  private double whatIsDouble() {
    return whatDouble;
  }

  @BeforeEach
  public void initEach() {
    pwm = new PWM(0);
  }

  @AfterEach
  public void closePWM() {
    pwm.close();
  }

  @Test
  @DisplayName("Test setting and getting and displaying the value.")
  void testSetValueAndGetValue() {
    BlinkinSubsystem blinkin = new BlinkinSubsystem(pwm);
    double expectedValue = 0.5;
    blinkin.setValue(expectedValue);
    blinkin.periodic();
    /* The current value should be what we set it to. */
    assertThat(blinkin.getValue()).isEqualTo(expectedValue);
    /* We haven't enabled it yet, so the raw value should still be zero. */
    assertThat(blinkin.getSpeed()).isZero();
    blinkin.enable();
    blinkin.periodic();
    /* Enabled now, so the raw value should be the right value. */
    assertThat(blinkin.getSpeed()).isEqualTo(expectedValue);
    blinkin.disable();
    blinkin.periodic();
    /* Disabled again. The raw value goes to zero, but the current value remains the same. */
    assertThat(blinkin.getSpeed()).isZero();
    assertThat(blinkin.getValue()).isEqualTo(expectedValue);
    /* Try a value that is out of bounds. */
    blinkin.enable();
    assertThatThrownBy(
            () -> {
              blinkin.setValue(2.0);
            })
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("Test BooleanCondition constructor")
  void testBooleanConditionConstructor() {
    final double trueValue = 1.0;
    final double falseValue = 0.1;

    /* Get a new blinkin with the boolean lambda expression. */
    BlinkinSubsystem blinkin = new BlinkinSubsystem(pwm, this::isMainTrue, trueValue, falseValue);
    /* Disabled, so the raw value should be zero. */
    blinkin.periodic();
    assertThat(blinkin.getSpeed()).isZero();
    /* Enable. The boolean is false. */
    blinkin.enable();
    blinkin.periodic();
    assertThat(blinkin.getSpeed()).isEqualTo(mainState ? trueValue : falseValue);
    /* change mainState */
    mainState = !mainState;
    blinkin.periodic();
    assertThat(blinkin.getSpeed()).isEqualTo(mainState ? trueValue : falseValue);
    /* Disable. */
    blinkin.disable();
    blinkin.periodic();
    assertThat(blinkin.getSpeed()).isZero();
  }

  @Test
  @DisplayName("Test DoubleSupplier constructor")
  void testDoubleConstructor() {

    /* Get a new blinkin with the boolean lambda expression. */
    BlinkinSubsystem blinkin = new BlinkinSubsystem(pwm, this::whatIsDouble);
    whatDouble = 0.5;
    /* Disabled, so the raw value should be zero. */
    blinkin.periodic();
    assertThat(blinkin.getSpeed()).isZero();
    /* Enable. The whatDouble is zero . */
    blinkin.enable();
    whatDouble = 0.0;
    blinkin.periodic();
    assertThat(blinkin.getSpeed()).isZero();
    /* change whatDouble */
    whatDouble = 0.5;
    blinkin.periodic();
    assertThat(blinkin.getSpeed()).isEqualTo(whatDouble);
    /* change whatDouble again */
    whatDouble = 0.3;
    blinkin.periodic();
    assertThat(blinkin.getSpeed()).isEqualTo(whatDouble);
    /* Disable. */
    blinkin.disable();
    blinkin.periodic();
    assertThat(blinkin.getSpeed()).isZero();
    /* Test for value out of bounds */
    whatDouble = 1.5;
    blinkin.enable();
    assertThatThrownBy(
            () -> {
              blinkin.periodic();
            })
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("Test Alliance Display")
  void testAllianceDisplay() {
    BlinkinSubsystem blinkin = new BlinkinSubsystem(pwm);
    blinkin.enable();
    blinkin.periodic();
    assertThat(blinkin.getSpeed()).isZero();
    blinkin.setDisplayAlliance(true);
    blinkin.periodic();
    assertThat(blinkin.getSpeed()).isEqualTo(BlinkinSubsystem.RED);
    DriverStationSim.setAllianceStationId(AllianceStationID.Blue2);
    DriverStationSim.notifyNewData();
    blinkin.periodic();
    assertThat(blinkin.getSpeed()).isEqualTo(BlinkinSubsystem.BLUE);
  }
}
