/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2014 scd4j scd4j.tools@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.datamaio.scd4j.conf;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.datamaio.scd4j.tmpl.TemplateEngine;
import com.datamaio.scd4j.tmpl.TemplateEngineConfig;
import com.datamaio.scd4j.util.io.PathUtils;

/**
 *
 * @author Fernando Rubbo
 */
public class Configuration {
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static final String MODULES_FOLDER = "modules";
    public static final String CONFIG_FOLDER = "config";
    public static final String LOG_FOLDER = "log";
    public static final String BACKUP_FOLDER = "backup";

    public static final String HOOK_SUFFIX = ".hook";
    public static final String DELETE_SUFFIX = ".del";
    public static final String TEMPLATE_SUFFIX = ".tmpl";

    private final Install install;
    private final Settings settings;
    /** Dependencies. Where the key is the gradle dependency and the value is the file path */
    private final Map<String, Path> dependencies;
    private final Path projectDir;

    public static final Configuration build(final Path module) {
        final Install install = new Install(module);
        return new Configuration(install);
    }

    public static final Configuration build(final Path module, final Map<String, Object> props) {
        final Install install = new Install(module, props);
        return new Configuration(install);
    }

    private Configuration(final Install install) {
        this(install, new Settings(), new HashMap<>(), Paths.get(".").toAbsolutePath().toFile());
    }

    public Configuration(final Install install, final Settings settings, final Map<String, Path> dependencies, final File projectDir) {
        this.install = install;
        this.settings = settings;
        this.dependencies = dependencies;
        this.projectDir = projectDir != null ? projectDir.toPath() : null;
    }

    public Path getConfig() {
        return this.install.getConfig();
    }

    public Map<String, Object> getProps() {
        return this.install.getProps();
    }

    public Map<String, Object> getTempProps() {
        return this.install.getTempProps();
    }

    public Path getModule() {
        return this.install.getModules()[0];
    }

    public Path[] getModules() {
        return this.install.getModules();
    }

    public Env getEnv() {
        return this.install.getEnv();
    }

    public Path getDependency(final String name) {
        return this.dependencies.get(name);
    }

    public void prettyPrint() {
        Configuration.LOGGER.info("========================================================================================================");
        Configuration.LOGGER.info("======================================== Configuration =================================================");
        Configuration.LOGGER.info("========================================================================================================");
        Configuration.LOGGER.info(this.install.toString());
        Configuration.LOGGER.info(this.settings.toString());
        Configuration.LOGGER.info("========================================================================================================");
        Configuration.LOGGER.info("============ Properties allowed to be used during installation (i.e *.hook and *.tmpl) =================");
        Configuration.LOGGER.info("========================================================================================================");
        final Map<String, Object> props = this.getProps();
        final Set<String> keys = props.keySet();
        for (final String key : keys) {
            Configuration.LOGGER.info(String.format("%s = %s", key, props.get(key)));
        }
        Configuration.LOGGER.info("========================================================================================================");
    }

    public TemplateEngine getTemplateEngine() {
        final Template template = this.settings.getTemplate();
        final TemplateEngineConfig engineConfig = new TemplateEngineConfig(template.getEngine());
        return TemplateEngine.get(engineConfig);
    }

    public Path getLogFile() {
        final String currentExecutionPath = this.buildCurrentExecutionPath();
        final String fileName = currentExecutionPath + ".log";
        return PathUtils.get(this.workspace(), Configuration.LOG_FOLDER, fileName).normalize();
    }

    public Path getBackupDir() {
        final String currentExecutionPath = this.buildCurrentExecutionPath();
        return PathUtils.get(this.workspace(), Configuration.BACKUP_FOLDER, currentExecutionPath).normalize();
    }

    private Path workspace() {
        if (this.projectDir == null) {
            throw new IllegalStateException("Project base path ('projectDir') cannot be null!");
        }
        return
            // Paths.get(".").toAbsolutePath();
            this.projectDir.toAbsolutePath();
    }

    private String currentExecutionPath;

    private synchronized String buildCurrentExecutionPath() {
        if (this.currentExecutionPath == null) {
            final SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

            final Path workspace = this.workspace();
            String fileName = this.getConfig().getFileName().toString();
            if (fileName.endsWith(".conf")) {
                fileName = fileName.replace(".conf", "");
            }
            if (fileName.endsWith(".properties")) {
                fileName = fileName.replace(".properties", "");
            }
            final String module = workspace.relativize(this.getModule().toAbsolutePath()).toString().replace(Configuration.MODULES_FOLDER + File.separator, "");
            this.currentExecutionPath = Paths.get(module, fileName + "_" + df.format(new Date())).toString();
        }
        return this.currentExecutionPath;
    }
}