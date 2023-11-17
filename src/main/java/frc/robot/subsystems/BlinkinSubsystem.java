package frc.robot.subsystems;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.PWM;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

/**
 * Subsystem for controlling the Blinkin LED driver via PWM.
 *
 * <p>There are three modes of operation. One is where the caller sets the value to be displayed.
 * The second mode is a {@code Boolean} lambda expression is supplied when the Blinkin object is
 * constructed, along with a value to use when the expression is true and one when it is false. The
 * third mode is a {@code Double} lambda expression is supplied when the object is constructed and
 * the value of this expression is used as the value.
 *
 * <p>In any of the three modes the Blinkin can be set to display the alliance color. And it can be
 * enabled or disabled at any time.
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

  /** Set color to hot pink. */
  public static final double HOT_PINK = 0.57;

  /** Set color dark red. */
  public static final double DARK_RED = 0.59;

  /** Set color dark red. */
  public static final double RED = 0.61;

  /** Set color red-orange. */
  public static final double RED_ORANGE = 0.63;

  /** Set color orange. */
  public static final double ORANGE = 0.65;

  /** Set color gold. */
  public static final double GOLD = 0.67;

  /** Set color yellow. */
  public static final double YELLOW = 0.69;

  /** Set color lawn green. */
  public static final double LAWN_GREEN = 0.71;

  /** Set color lime. */
  public static final double LIME = 0.73;

  /** Set color dark green. */
  public static final double DARK_GREEN = 0.75;

  /** Set color green. */
  public static final double GREEN = 0.77;

  /** Set color blue-green. */
  public static final double BLUE_GREEN = 0.79;

  /** Set color aqua. */
  public static final double AQUA = 0.81;

  /** Set color sky blue. */
  public static final double SKY_BLUE = 0.83;

  /** Set color dark blue. */
  public static final double DARK_BLUE = 0.85;

  /** Set color blue. */
  public static final double BLUE = 0.87;

  /** Set color blue-violet. */
  public static final double BLUE_VIOLET = 0.89;

  /** Set color violet. */
  public static final double VIOLET = 0.91;

  /** Set color white. */
  public static final double WHITE = 0.93;

  /** Set color gray. */
  public static final double GRAY = 0.95;

  /** Set color dark gray. */
  public static final double DARK_GRAY = 0.97;

  /** Set color black. */
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
    this.ledController.setBoundsMicroseconds(2000, 1500, 1500, 1500, 1000);
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
   * Constructor that takes a PWM port number for the Blinkin LED driver. In this mode the Blinkin's
   * behavior is manually controlled by the caller. Use the {@code setValue()} method to set the
   * colors displayed. The colors are only displayed when the the Blinkin is enabled using the
   * {@code enable()} method. The Blinkin is initially disabled. The Blinkin can also be set to
   * display the alliance color by calling the {@code displayAlliance()} method with the value
   * {@code true}. The alliance display mode setting takes precedence over the current value
   * setting. The alliance display mode is turned off by calling {@code displayAlliance()} with the
   * value {@code false}.
   *
   * @param pwm the PWM for the Blinkin LED driver
   */
  public BlinkinSubsystem(PWM pwm) {
    this(pwm, (BooleanSupplier) null, 0.0, 0.0, (DoubleSupplier) null);
  }

  /**
   * Constructor that takes a Boolean lambda expression and two integers. The values must be in the
   * rage -1.0 to 1.0. You may use the provided color constants if the Blinkin is in the factory
   * default state.
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
   * Constructor that takes an double lambda expression to set the value each cycle. The values
   * returned must be in the range -1.0 to 1.0.
   *
   * @param pwm the PWM for the Blinkin LED driver
   * @param valueFunc the double lambda expression to set the value each cycle
   */
  public BlinkinSubsystem(PWM pwm, DoubleSupplier valueFunc) {
    this(pwm, (BooleanSupplier) null, 0.0, 0.0, valueFunc);
  }

  /**
   * Sets the value to display of the Blinkin LED driver. The value is only displayed if the object
   * is in the enabled state. The value is overridden by the {@code displayAlliance} setting, in
   * which case the alliance color is displayed instead.
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
      double colorToSet;

      Optional<Alliance> whichAlliance = DriverStation.getAlliance();

      switch (whichAlliance.isPresent() ? whichAlliance.get().toString() : "Black") {
        case "Red":
          colorToSet = RED;
          break;
        case "Blue":
          colorToSet = BLUE;
          break;
        case "Black":
          colorToSet = BLACK;
          break;
        default:
          throw new IllegalArgumentException(whichAlliance.toString());
      }
      this.ledController.setSpeed(colorToSet);
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

  /** Closes the PWM port, allowing it to be reused by a new object. */
  public void close() {
    this.ledController.close();
  }

  /**
   * Gets the currently displayed value. This may differ from the current display value depending on
   * the enabled state and whether {@code displayAlliance} is set to (@code true).
   *
   * @return The current value of the PWM port.
   */
  public double getSpeed() {
    return (this.ledController.getSpeed());
  }

  /**
   * Set the display alliance state.
   *
   * @param display When true, set color to match alliance.
   */
  public void setDisplayAlliance(boolean display) {
    this.displayAlliance = display;
  }
}
