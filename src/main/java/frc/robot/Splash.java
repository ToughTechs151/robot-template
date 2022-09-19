package frc.robot;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@java.lang.SuppressWarnings("squid:S106")
class Splash {

  private Splash() {}

  public static boolean printAllStatusFiles() {
    final String Sepline = "=====================================================================";

    // Print the Splash Screen
    System.out.println(Sepline);
    System.out.println(Sepline);
    System.out.println(Sepline);
    System.out.println(Sepline);
    System.out.println("Starting robotInit for Tough Techs");
    printStatusFile("deployhost.txt", true, 0, 2, 1);
    printStatusFile("deploytime.txt", true, 0, 3, 2);
    printStatusFile("buildtime.txt", false, 0, 0, 2);
    printStatusFile("branch.txt", false, 0, 5, 1);
    printStatusFile("commit.txt", false, 1, 0, 10);
    printStatusFile("changes.txt", false, 2, 0, 10);
    printStatusFile("remote.txt", false, 3, 0, 10);
    printStatusFile("user.txt", false, 4, 0, 10);
    System.out.println(Sepline);

    return true;
  }

  @java.lang.SuppressWarnings("squid:S2095")
  private static void printStatusFile(
      String filename, Boolean isDeploy, int rowIndex, int colIndex, int widthIndex) {
    byte[] buffer = new byte[1024];
    InputStream statusfile = null;
    ShuffleboardTab tab;
    NetworkTableEntry field;
    try {
      if (Boolean.TRUE.equals(isDeploy)) {
        if (RobotBase.isSimulation()) {
          statusfile =
              new BufferedInputStream(
                  new FileInputStream(
                      Filesystem.getLaunchDirectory() + "/src/main/deploy/" + filename));
        } else {
          statusfile =
              new BufferedInputStream(
                  new FileInputStream(Filesystem.getDeployDirectory() + "/" + filename));
        }
      } else {
        statusfile = Main.class.getResourceAsStream("/" + filename);
      }
      System.out.print((filename + ": ").replace(".txt", ""));

      try {
        for (int length = 0; (length = statusfile.read(buffer)) != -1; ) {
          String buf = new String(buffer, StandardCharsets.UTF_8).replaceAll("\\s", " ");
          String tfn = filename.replace(".txt", "");
          String fn = tfn.substring(0, 1).toUpperCase() + tfn.substring(1);
          System.out.write(buffer, 0, length);
          SmartDashboard.putString(fn, buf);
          tab = Shuffleboard.getTab("Status");
          field =
              tab.add(fn, buf).withPosition(colIndex, rowIndex).withSize(widthIndex, 1).getEntry();
          field.setString(buf);
        }
      } finally {
        System.out.println();
      }
      statusfile.close();

    } catch (Exception e) {
      System.out.println("Unable to find file.");
      System.out.println(e.getMessage());
    }
    try {
      if (statusfile != null) {
        statusfile.close();
      }
    } catch (Exception ignore) {
      // Ignore nullpointer exception from statusfile.
    }
  }
}
