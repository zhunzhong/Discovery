package com.nepxion.discovery.plugin.strategy.starter.agent.core.plugin;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 *
 * @author zifeihan
 * @version 1.0
 */

import com.nepxion.discovery.plugin.strategy.starter.agent.core.callback.TransformTemplate;
import com.nepxion.discovery.plugin.strategy.starter.agent.core.logger.AgentLogger;
import com.nepxion.discovery.plugin.strategy.starter.agent.core.plugin.loader.PluginLoader;
import com.nepxion.discovery.plugin.strategy.starter.agent.core.plugin.loader.URLClassLoaderFactory;
import com.nepxion.discovery.plugin.strategy.starter.agent.core.plugin.thread.ThreadPlugin;
import com.nepxion.discovery.plugin.strategy.starter.agent.core.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PluginFinder {

    private static final AgentLogger LOG = AgentLogger.getLogger(PluginFinder.class.getName());

    public static void load(TransformTemplate transformTemplate) {
        new ThreadPlugin().install(transformTemplate);

        URL[] pluginUrls = getPlugin().toArray(new URL[]{});
        ClassLoader classLoader = URLClassLoaderFactory.createClassLoader("discovery.agent",
                pluginUrls, PluginFinder.class.getClassLoader());
        List<Plugin> loadPlugins = PluginLoader.load(classLoader, Plugin.class);
        for (Plugin plugin : loadPlugins) {
            plugin.install(transformTemplate);
            LOG.info(String.format("%s install successfully", plugin.getClass().getSimpleName()));
        }
    }

    public static List<URL> getPlugin() {
        File agentDictionary = AgentPath.getPath();
        File plugins = new File(agentDictionary, "plugin");
        return resolveLib(plugins.getAbsolutePath());
    }

    private static List<URL> resolveLib(String agentLibPath) {
        final File libDir = new File(agentLibPath);
        if (checkDirectory(libDir)) {
            return Collections.emptyList();
        }
        final File[] libFileList = FileUtils.listFiles(libDir, new String[]{".jar"});

        List<URL> libURLList = toURLs(libFileList);
        URL agentDirUri = toURL(new File(agentLibPath));

        List<URL> jarURLList = new ArrayList<URL>(libURLList);
        jarURLList.add(agentDirUri);
        return jarURLList;
    }

    private static boolean checkDirectory(File file) {
        if (!file.exists()) {
            LOG.warn(file + " not found");
            return true;
        }
        if (!file.isDirectory()) {
            LOG.warn(file + " is not a directory");
            return true;
        }
        return false;
    }

    private static List<URL> toURLs(File[] jarFileList) {
        try {
            URL[] jarURLArray = FileUtils.toURLs(jarFileList);
            return Arrays.asList(jarURLArray);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static URL toURL(File file) {
        try {
            return FileUtils.toURL(file);
        } catch (IOException e) {
            LOG.warn(file.getName() + ".toURL() failed.", e);
            throw new RuntimeException(file.getName() + ".toURL() failed.", e);
        }
    }
}