package com.helger.phoss.smp.backend.sql.mgr;

import javax.annotation.Nonnull;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.internal.jdbc.DriverDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.string.StringHelper;
import com.helger.phoss.smp.SMPServerConfiguration;
import com.helger.phoss.smp.backend.sql.EDatabaseType;
import com.helger.phoss.smp.backend.sql.SMPJDBCConfiguration;
import com.helger.phoss.smp.backend.sql.migration.V2__MigrateDBUsersToPhotonUsers;
import com.helger.settings.exchange.configfile.ConfigFile;

/**
 * This class has the sole purpose of encapsulating the org.flywaydb classes, so
 * that it's usage can be turned off (for whatever reason).
 *
 * @author Philip Helger
 */
final class FlywayMigrator
{
  private static final Logger LOGGER = LoggerFactory.getLogger (FlywayMigrator.Singleton.class);

  // Indirection level to not load org.flyway classes by default
  public static final class Singleton
  {
    static final FlywayMigrator INSTANCE = new FlywayMigrator ();
  }

  private FlywayMigrator ()
  {}

  void runFlyway (@Nonnull final EDatabaseType eDBType)
  {
    ValueEnforcer.notNull (eDBType, "DBType");

    final ConfigFile aCF = SMPServerConfiguration.getConfigFile ();
    final FluentConfiguration aConfig = Flyway.configure ()
                                              .dataSource (new DriverDataSource (FlywayMigrator.class.getClassLoader (),
                                                                                 aCF.getAsString (SMPJDBCConfiguration.CONFIG_JDBC_DRIVER),
                                                                                 aCF.getAsString (SMPJDBCConfiguration.CONFIG_JDBC_URL),
                                                                                 aCF.getAsString (SMPJDBCConfiguration.CONFIG_JDBC_USER),
                                                                                 aCF.getAsString (SMPJDBCConfiguration.CONFIG_JDBC_PASSWORD)))
                                              // Required for creating DB table
                                              .baselineOnMigrate (true)
                                              // Disable validation, because
                                              // DDL comments are also taken
                                              // into consideration
                                              .validateOnMigrate (false)
                                              // Version 1 is the baseline
                                              .baselineVersion ("1")
                                              .baselineDescription ("SMP 5.2.x database layout, MySQL only")
                                              // Separate directory per DB type
                                              .locations ("db/migrate-" + eDBType.getID ())
                                              /*
                                               * Avoid scanning the ClassPath by
                                               * enumerating them explicitly
                                               */
                                              .javaMigrations (new V2__MigrateDBUsersToPhotonUsers ());

    // Flyway to handle the DB schema?
    final String sSchema = aCF.getAsString (SMPJDBCConfiguration.CONFIG_JDBC_SCHEMA);
    if (StringHelper.hasText (sSchema))
    {
      // Use the schema only, if it is explicitly configured
      // The default schema name is ["$user", public] and as such unusable
      aConfig.schemas (sSchema);
    }

    // If no schema is specified, schema create should also be disabled
    final boolean bCreateSchema = aCF.getAsBoolean (SMPJDBCConfiguration.CONFIG_JDBC_SCHEMA_CREATE, false);
    aConfig.createSchemas (bCreateSchema);

    final Flyway aFlyway = aConfig.load ();
    if (false)
      aFlyway.validate ();
    aFlyway.migrate ();

    LOGGER.info ("Finished running Flyway");
  }
}
