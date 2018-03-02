package com.szh.tantanphoto.dragalbum.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 
 * <p>
 * Title: SharedPreferences封装类
 * </p>
 * <p>
 * Description:一个简单的SharedPreferences 保存封装
 * </p>
 * <p>
 * Copyright: Copyright (c) 2014
 * </p>
 * 
 * @author 史章华 QQ1095897632
 * @date 2014年4月6日
 * @version 1.0
 */
public class SP2 {
	private static SP2 _instance = null;
	SharedPreferences sp;
	SharedPreferences.Editor editor;
	private static String SpName = null;
	private final String MAK = "<@Ligoudan>{W|Q|N|M|L|G|B}"; // AES加密的密钥 @李狗蛋 我去年买了个表 0.o 
	@SuppressWarnings("deprecation")
	private final int MODE = Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE;

	public SharedPreferences.Editor getEditor() {
		return editor;
	}

	private SP2(Context c) {
		sp = c.getSharedPreferences("config", MODE);
		editor = sp.edit();
	}

	private SP2(Context c, String name) {
		sp = c.getSharedPreferences(name, MODE);
		editor = sp.edit();
	}

	// 单例静态工厂方法,同步防止多线程环境同时执行
	synchronized public static SP2 getInstance(Context c) {
		if (_instance == null || SpName != null) {
			SpName = null;
			_instance = new SP2(c);
		}
		return _instance;
	}

	/**
	 * 
	 * @param c
	 *            Context
	 * @param name
	 *            文件名
	 * @return
	 */
	synchronized public static SP2 getInstance(Context c, String name) {
		if (SpName == null || !SpName.equals(name)) {
			_instance = new SP2(c, name);
		}
		return _instance;
	}

	public SharedPreferences getSp() {
		return sp;
	}

	public boolean getBoolean(String key, boolean value) {
		try {
			return sp.getBoolean(key, value);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return value;
	}

	public int getInt(String key, int value) {
		try {
			return sp.getInt(key, value);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return value;
	}

	public long getLong(String key, long value) {
		try {
			return sp.getLong(key, value);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return value;
	}

	public String getString(String key, String value) {
		String str = null;
		try {
			str = sp.getString(key, null);
			if (str != null && !"".equals(str)) {
				str = AESEncryptor.decrypt(MAK, str);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}

	public float getFloat(String key, float value) {
		try {
			return sp.getFloat(key, value);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return value;
	}

	public void putBoolean(String key, boolean value) {
		editor.putBoolean(key, value);
		editor.commit();
	}

	public void putLong(String key, long value) {
		editor.putLong(key, value);
		editor.commit();
	}

	public void putInt(String key, int value) {
		editor.putInt(key, value);
		editor.commit();
	}

	public void putFloat(String key, float value) {
		editor.putFloat(key, value);
		editor.commit();
	}

	public void putString(String key, String value) {
		try {
			editor.putString(key, AESEncryptor.encrypt(MAK, value));
		} catch (Exception e) {
			editor.putString(key, value);
		}
		editor.commit();
	}

	public boolean removeKey(String key) {
		editor.remove(key);
		return editor.commit();
	}

	public boolean removeAllKey() {
		editor.clear();
		return editor.commit();
	}
}
