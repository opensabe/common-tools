package io.github.opensabe.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * properties甯姪绫�
 * 榛樿鍔犺浇config.properties
 */
public class PropertyUtils
{
	private static Log log = LogFactory.getLog(PropertyUtils.class);

	private static final String config = "config.properties";

	private static Map<String, String> config_map = new HashMap<String, String>();

	static
	{
		load(config);
	}

	public static String getProperty(String key)
	{
		if (StringUtils.isBlank(key))
		{
			return null;
		}
		return config_map.get(key);
	}

	public static String getProperty(String key, String defaultValue)
	{
		if (StringUtils.isEmpty(key))
		{
			return (StringUtils.isEmpty(defaultValue) ? null : defaultValue);
		}
		return (StringUtils.isEmpty(config_map.get(key)) ? defaultValue : config_map.get(key));
	}

	public static int getPropertyIntValue(String key, int defaultValue)
	{
		if (StringUtils.isEmpty(key))
		{
			return defaultValue;
		}
		return (StringUtils.isEmpty(config_map.get(key)) || !isInt(config_map.get(key))) ? defaultValue : Integer
				.parseInt(config_map.get(key));
	}

	private static boolean isInt(String n)
	{
		try
		{
			Integer.parseInt(n);
			return true;
		}
		catch (Throwable e)
		{
			return false;
		}
	}

	@SuppressWarnings("rawtypes")
	private static void load(String name)
	{
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
		Properties p = new Properties();
		try
		{
			if (is != null)
			{
				p.load(is);
			}
			if (config.equals(name))
			{
				for (Map.Entry e : p.entrySet())
				{
					config_map.put((String) e.getKey(), (String) e.getValue());
				}
			}

		}
		catch (IOException e)
		{
			log.error("load property file failed. file name: " + name, e);
		}
	}
}