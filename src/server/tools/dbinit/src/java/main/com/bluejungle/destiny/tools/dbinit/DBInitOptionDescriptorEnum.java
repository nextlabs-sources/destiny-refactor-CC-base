package com.bluejungle.destiny.tools.dbinit;

import java.io.File;

import com.bluejungle.framework.utils.ArrayUtils;
import com.bluejungle.version.IVersion;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptorTree;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.OptionValueType;
import com.nextlabs.shared.tools.impl.Option;
import com.nextlabs.shared.tools.impl.OptionDescriptorTreeImpl;
import com.nextlabs.shared.tools.impl.SequencedListOptionDescriptor;
import com.nextlabs.shared.tools.impl.UniqueChoiceOptionDescriptor;

/**
 * TODO description
 *
 * @author hchan
 * @date Jun 28, 2007
 */
class DBInitOptionDescriptorEnum implements IConsoleApplicationDescriptor {
    /*
     *  - Unique
     - [Simple] # [h]
     - Sequence
     - Unique
     - Simple # install
     - Sequence
     - Simple # upgrade
     - Simple # fromV <1.6.0/2.0.0/2.5.0>
     - Simple # toV <1.6.0/2.0.0/2.5.0/3.0.0>
     - Sequence
     - Unique
     - Simple # createschema
     - Simple # dropcreateschema
     - Simple # updateschema
     - Simple # processsql
     - Simple # schema <schema path>
     - Simple # config <config path>
     - Simple # connection <configuration.xml path>
     - Simple # libraryPath <libraryPath>
     - [Simple] # [quiet]
    */

    static final OptionId<Boolean> INSTALL_OID              = OptionId.create("install",          OptionValueType.ON_OFF);
    static final OptionId<Boolean> UPGRADE_OID              = OptionId.create("upgrade",          OptionValueType.ON_OFF);
    static final OptionId<Boolean> CREATE_SCHEMA_OID        = OptionId.create("createschema",     OptionValueType.ON_OFF);
    static final OptionId<Boolean> DROP_CREATE_SCHEMA_OID   = OptionId.create("dropcreateschema", OptionValueType.ON_OFF);
    static final OptionId<Boolean> UPDATE_SCHEMA_OID        = OptionId.create("updateschema",     OptionValueType.ON_OFF);
    static final OptionId<Boolean> PROCESS_SQL_FILE_OID     = OptionId.create("sqlfile",          OptionValueType.ON_OFF);

    static final OptionId<File>    CONFIG_OID               = OptionId.create("config",           OptionValueType.EXIST_FILE);
    static final OptionId<File>    SCHEMA_OPTION_ID         = OptionId.create("schema",           OptionValueType.FILE);
    static final OptionId<File>    SERVER_INSTALL_HOME_OID  = OptionId.create("connection",       OptionValueType.EXIST_FOLDER);
    static final OptionId<String>  LIBRARY_PATH_OID         = OptionId.create("libraryPath",      OptionValueType.STRING);
    static final OptionId<Boolean> QUIET_OID                = OptionId.create("quiet",            OptionValueType.ON_OFF);
    static final OptionId<String>  FROM_VERSION_OID         = OptionId.create("fromV",            OptionValueType.STRING);
    static final OptionId<String>  TO_VERSION_OID           = OptionId.create("toV",              OptionValueType.STRING);

    private IOptionDescriptorTree options;

    DBInitOptionDescriptorEnum() throws InvalidOptionDescriptorException{
        SequencedListOptionDescriptor root = new SequencedListOptionDescriptor();
        UniqueChoiceOptionDescriptor actionGroup = new UniqueChoiceOptionDescriptor();

        IOptionDescriptor<?> option = Option.createOnOffOption(INSTALL_OID, "install");
        option.getCommandLineIndicators().add("i");
        actionGroup.add(option);

        SequencedListOptionDescriptor upgradeSeq = new SequencedListOptionDescriptor();
        option = Option.createOnOffOption(UPGRADE_OID, "upgrade");
        option.getCommandLineIndicators().add("u");
        upgradeSeq.add(option);

        String description = "upgrade from version, only required if upgrade";
        option = Option.createOption(FROM_VERSION_OID, description, "from version");
        option.getCommandLineIndicators().add("f");
        upgradeSeq.add(option);

        description = "upgrade to version, only required if upgrade";
        option = Option.createOption(TO_VERSION_OID, description, "to version");
        option.getCommandLineIndicators().add("t");
        upgradeSeq.add(option);
        actionGroup.add(upgradeSeq);

        SequencedListOptionDescriptor otherActionSeq = new SequencedListOptionDescriptor();
        UniqueChoiceOptionDescriptor otherActionGroup = new UniqueChoiceOptionDescriptor();
        option = Option.createOnOffOption(CREATE_SCHEMA_OID, "output a create sql to a file");
        otherActionGroup.add(option);

        option = Option.createOnOffOption(DROP_CREATE_SCHEMA_OID,
                             "output a drop-create sql to a file, it will drop all the table first before create");
        otherActionGroup.add(option);

        option = Option.createOnOffOption(UPDATE_SCHEMA_OID, "output a update sql to a file");
        otherActionGroup.add(option);

        option = Option.createOnOffOption(PROCESS_SQL_FILE_OID, "process sql from a file");
        otherActionGroup.add(option);

        otherActionSeq.add(otherActionGroup);

        option = Option.createOption(SCHEMA_OPTION_ID, "-path/to/schema", "schema path");
        option.getCommandLineIndicators().add("s");
        otherActionSeq.add(option);

        actionGroup.add(otherActionSeq);
        root.add(actionGroup);


        option = Option.createOption(CONFIG_OID, "-path/to/config", "config path");
        option.getCommandLineIndicators().add("c");
        root.add(option);


        option = Option.createOption(SERVER_INSTALL_HOME_OID,
                                     "path to Control Center Folder. Such as \"c:\\Program File\\Nextlabs\\Policy Server\\\"",
                                     "SERVER_HOME");
        option.getCommandLineIndicators().add("C");
        root.add(option);

        option = Option.createOption(LIBRARY_PATH_OID, "library path", "libraryPath");
        option.getCommandLineIndicators().add("L");
        root.add(option);

        option = Option.createOnOffOption(QUIET_OID, "quiet, default is false");
        option.getCommandLineIndicators().add("q");
        root.add(option);

        options = new OptionDescriptorTreeImpl(root);
    }

    /* (non-Javadoc)
     * @see com.nextlabs.shared.tools.IConsoleApplicationDescriptor#getLongDescription()
     */
    public String getLongDescription() {
        return "TODO DBInit long description";
    }

    /* (non-Javadoc)
     * @see com.nextlabs.shared.tools.IConsoleApplicationDescriptor#getName()
     */
    public String getName() {
        return "dbinit";
    }

    /* (non-Javadoc)
     * @see com.nextlabs.shared.tools.IConsoleApplicationDescriptor#getOptions()
     */
    public IOptionDescriptorTree getOptions() {
        return options;
    }

    /* (non-Javadoc)
     * @see com.nextlabs.shared.tools.IConsoleApplicationDescriptor#getShortDescription()
     */
    public String getShortDescription() {
        return "TODO DBInit short description";
    }

}
