package frc.robot.subsystems;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.PWM;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

/**
 * Subsystem for controlling the Blinkin LED driver via PWM.
 *
 * <p>The color constants in this class only apply when the Blinkin is in the factory reset state.
 * To restore to factory reset do the following:
 *
 * <ol>
 *   <li>Power off the Blinkin.
 *   <li>Press and hold the Mode and Strip Select buttons.
 *   <li>Power on the Blinkin.
 *   <li>Wait for about two seconds.
 *   <li>Release the Mode and Strip Select buttons.
 * </ol>
 *
 * <p>These are the Factory default EEPROM values:
 *
 * <ul>
 *   <li>Color 1 - Sky Blue - 0x0080FF
 *   <li>Color 2 - Gold - 0xFFEA00
 *   <li>Strip Length - 60 LEDs
 *   <li>No Signal Pattern - 29 - Color Waves, Party Palette
 *   <li>Strip Select - 5V.
 * </ul>
 *
 * @see <a href="https://www.revrobotics.com/content/docs/REV-11-1105-UM.pdf">Blinkin User Guide</a>
 */
public class BlinkinSubsystem extends SubsystemBase {

  private PWM ledController;

  private double currentValue;
  private boolean isEnabled;
  private boolean displayAlliance = false;
  private final BooleanSupplier condition;
  private final DoubleSupplier valueFunc;
  private final double trueValue;
  private final double falseValue;

  /* Color Constants when in factory default state. */

  public static final double HOT_PINK = 0.57;
  public static final double DARK_RED = 0.59;
  public static final double RED = 0.61;
  public static final double RED_ORANGE = 0.63;
  public static final double ORANGE = 0.65;
  public static final double GOLD = 0.67;
  public static final double YELLOW = 0.69;
  public static final double LAWN_GREEN = 0.71;
  public static final double LIME = 0.73;
  public static final double DARK_GREEN = 0.75;
  public static final double GREEN = 0.77;
  public static final double BLUE_GREEN = 0.79;
  public static final double AQUA = 0.81;
  public static final double SKY_BLUE = 0.83;
  public static final double DARK_BLUE = 0.85;
  public static final double BLUE = 0.87;
  public static final double BLUE_VIOLET = 0.89;
  public static final double VIOLET = 0.91;
  public static final double WHITE = 0.93;
  public static final double GRAY = 0.95;
  public static final double DARK_GRAY = 0.97;
  public static final double BLACK = 0.99;

  private BlinkinSubsystem(
      PWM pwm,
      BooleanSupplier condition,
      double trueValue,
      double falseValue,
      DoubleSupplier valueFunc) {
    this.ledController = pwm;
    /* Set up the widths to look like a Spark Controller and eliminate the deadband. */
    /* The next line is from the spark controller code. */
    /* this.ledController.setBounds(2.003, 1.55, 1.50, 1.46, 0.999)
    /* The next line sets the widths as defined in the BLinkin manual. */
    this.ledController.setBounds(2000.0, 1500.0, 1500.0, 1500.0, 1000);
    this.ledController.enableDeadbandElimination(false);
    this.ledController.setPeriodMultiplier(PWM.PeriodMultiplier.k1X);
    this.ledController.setSpeed(0.0);
    this.ledController.setZeroLatch();
    this.ledController.setDisabled();
    this.currentValue = 0.0;
    this.isEnabled = false;
    this.condition = condition;
    this.trueValue = trueValue;
    this.falseValue = falseValue;
    this.valueFunc = valueFunc;
    this.ledController.setDisabled();
  }

  /**
   * Constructor that takes a PWM port number for the Blinkin LED driver.
   *
   * @param pwm the PWM for the Blinkin LED driver
   */
  public BlinkinSubsystem(PWM pwm) {
    this(pwm, (BooleanSupplier) null, 0.0, 0.0, (DoubleSupplier) null);
  }

  /**
   * Constructor that takes a Boolean lambda expression and two integers.
   *
   * @param pwm the PWM for the Blinkin LED driver
   * @param condition the Boolean lambda expression to determine which integer value to set
   * @param trueValue the value to set if the condition lambda returns true
   * @param falseValue the value to set if the condition lambda returns false
   */
  public BlinkinSubsystem(PWM pwm, BooleanSupplier condition, double trueValue, double falseValue) {
    this(pwm, condition, trueValue, falseValue, (DoubleSupplier) null);
  }

  /**
   * Constructor that takes an double lambda expression to set the value each cycle.
   *
   * @param pwm the PWM for the Blinkin LED driver
   * @param valueFunc the double lambda expression to set the value each cycle
   */
  public BlinkinSubsystem(PWM pwm, DoubleSupplier valueFunc) {
    this(pwm, (BooleanSupplier) null, 0.0, 0.0, valueFunc);
  }

  /**
   * Sets the value of the Blinkin LED driver.
   *
   * @param value the value to set (0.0-1.0 range)
   */
  public void setValue(double value) throws IllegalArgumentException {
    if (value <= -1.0 || value >= 1.0) {
      throw new IllegalArgumentException(Double.toString(value));
    }
    this.currentValue = value;
  }

  /**
   * Returns the current value of the Blinkin LED driver.
   *
   * @return the current value (0.0-1.0 range)
   */
  public double getValue() {
    return this.currentValue;
  }

  /** Enables the Blinkin LED driver. */
  public void enable() {
    this.isEnabled = true;
  }

  /** Disables the Blinkin LED driver. */
  public void disable() {
    this.isEnabled = false;
    this.ledController.setDisabled();
  }

  @Override
  public void periodic() throws IllegalArgumentException {
    /*
     * Currently the PWM simulation code maintains separate values for the PWM when set by setSpeed,
     * setPosition and setSpeed. Because of this we have to be careful to always use only one pair
     * of get and set options so the simulator works.
     */
    if (!isEnabled) {
      return;
    }

    if (displayAlliance) {
      Alliance whichAlliance = DriverStation.getAlliance();
      switch (whichAlliance) {
        case Red:
          this.ledController.setSpeed(RED);
          break;
        case Blue:
          this.ledController.setSpeed(BLUE);
          break;
        case Invalid:
          this.ledController.setSpeed(BLACK);
          break;
        default:
          throw new IllegalArgumentException(whichAlliance.toString());
      }
      return;
    }

    if (condition != null) {
      this.ledController.setSpeed(condition.getAsBoolean() ? this.trueValue : this.falseValue);
    } else if (valueFunc != null) {
      double newValue = valueFunc.getAsDouble();
      if (newValue <= -1.0 || newValue >= 1.0) {
        throw new IllegalArgumentException(Double.toString(newValue));
      }
      this.ledController.setSpeed(newValue);
    } else {
      this.ledController.setSpeed(this.currentValue);
    }
  }

  public void close() {
    this.ledController.close();
  }

  public double getSpeed() {
    return (this.ledController.getSpeed());
  }

  public void setDisplayAlliance(boolean display) {
    this.displayAlliance = display;
  }
}
