/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2014 scd4j scd4j.tools@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.datamaio.scd4j.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.datamaio.scd4j.conf.Configuration;

/**
 * 
 * @author Fernando Rubbo
 */
public class LogHelper
{
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private final Configuration conf;

	public LogHelper(Configuration conf) {
		this.conf = conf;
	}

	public void startup() {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %1$tb %1$td, %1$tY %1$tH:%1$tM:%1$tS %5$s%6$s%n");

		// avoid tha the handler be registred more than once
		Handler[] handlers = LOGGER.getHandlers();
		for (Handler handler : handlers) {
			if (handler instanceof FileHandler) {
				// it is required to close in order to avoid .lck file
				FileHandler fh = (FileHandler) handler;
				fh.close();
			}
			LOGGER.removeHandler(handler);
		}

        // registry the handlers
        LOGGER.setLevel(Level.INFO);
        LOGGER.setUseParentHandlers(false);

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter());
        LOGGER.addHandler(consoleHandler);

		Path file = conf.getLogFile();
		String logFileName = file.toString();
		try {
			if (!Files.exists(file.getParent())) {
				Files.createDirectories(file.getParent());
			}

			FileHandler fileHandler = new FileHandler(file.toString());
			fileHandler.setFormatter(new SimpleFormatter());
			LOGGER.addHandler(fileHandler);
		} catch (Exception e) {
			throw new RuntimeException("Erro criarndo arquivo de log " + logFileName, e);
		}
    }
}

