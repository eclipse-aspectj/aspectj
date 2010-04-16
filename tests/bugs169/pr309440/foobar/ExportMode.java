package foobar;

import javax.annotation.Nonnull;

public enum ExportMode
{
  /**
   * export configuration and project data
   */
  COMPLETE("Complete"),

  /**
   * exports configuration only
   */
  CONFIGURATION_ONLY("Configuration only");

  /**
   * The display-name
   */
  private final String mDisplayName;

  /**
   * Stores the displayName
   * @param aDisplayName
   */
  ExportMode(@Nonnull String aDisplayName)
  {
    mDisplayName = aDisplayName;
  }

  /**
   * 
   * @return the display name
   */
  public @Nonnull
  String getDisplayName()
  {
    return mDisplayName;
  }

}
