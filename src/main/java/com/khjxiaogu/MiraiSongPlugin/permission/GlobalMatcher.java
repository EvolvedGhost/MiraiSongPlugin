/**
 * Mirai Song Plugin
 * Copyright (C) 2021  khjxiaogu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.MiraiSongPlugin.permission;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import com.khjxiaogu.MiraiSongPlugin.MiraiSongPlugin;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;

public class GlobalMatcher implements PermissionMatcher {
	BotMatcher global;
	Map<Long, BotMatcher> local = new ConcurrentHashMap<>();

	@Override
	public PermissionResult match(MatchInfo info) {
		BotMatcher bm = local.getOrDefault(info.bot.getId(), global);
		return bm.match(info);
	}

	public boolean loadString(String s, Bot b) {
		BotMatcher bm = local.computeIfAbsent(b.getId(), x -> new BotMatcher());
		if (bm.loadMatcher(s, 0)) {
			try (FileOutputStream fis = new FileOutputStream(new File(loadfrom, b.getId() + ".permission"), true);
					PrintStream sc = new PrintStream(fis)) {
				sc.println();
				sc.print(s);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return true;
		}
		return false;
	}

	public boolean loadString(String s) {
		if (global.loadMatcher(s, 0)) {
			try (FileOutputStream fis = new FileOutputStream(new File(loadfrom, "global.permission"), true);
					PrintStream sc = new PrintStream(fis)) {
				sc.println();
				sc.print(s);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return true;
		}
		return false;
	}

	public void rebuildConfig() {
		try (FileOutputStream fis = new FileOutputStream(new File(loadfrom, "global.permission"), false);
				PrintStream sc = new PrintStream(fis)) {
			boolean nfirst = false;
			for (String s : global.getValue()) {
				if (nfirst)
					sc.println();
				nfirst = true;
				sc.print(s);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		for (Entry<Long, BotMatcher> i : local.entrySet()) {
			try (FileOutputStream fis = new FileOutputStream(new File(loadfrom, i.getKey() + ".permission"), false);
					PrintStream sc = new PrintStream(fis)) {
				boolean nfirst = false;
				for (String s : i.getValue().getValue()) {
					if (nfirst)
						sc.println();
					nfirst = true;
					sc.print(s);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	File loadfrom;

	public void load(File f) {
		loadfrom = f;
		global = null;
		local.clear();
		File gc = new File(f, "global.permission");
		global = new BotMatcher();
		try (FileInputStream fis = new FileInputStream(gc); Scanner sc = new Scanner(fis)) {
			int i = 0;
			while (sc.hasNextLine()) {
				i++;
				try {
					global.loadMatcher(sc.nextLine(), 0);
				} catch (Exception ex) {
					MiraiSongPlugin.getMLogger().warning(ex);
					MiraiSongPlugin.getMLogger().warning("权限配置文件" + gc.getName() + "的第" + i + "行有语法错误！");
				}
			}
		} catch (Exception ex) {
			MiraiSongPlugin.getMLogger().warning(ex);
			MiraiSongPlugin.getMLogger().warning("权限配置文件" + gc.getName() + "读取失败！" + ex.getMessage());

		}
		for (File ff : f.listFiles()) {
			try {
				if (ff.getName().endsWith(".permission")) {
					String fn = ff.getName().split("\\.")[0];
					if (Character.isDigit(fn.charAt(0))) {
						long gn = Long.parseLong(fn);
						BotMatcher bm = new BotMatcher();
						try (FileInputStream fis = new FileInputStream(ff); Scanner sc = new Scanner(fis)) {
							int i = 0;
							while (sc.hasNextLine()) {
								i++;
								try {
									bm.loadMatcher(sc.nextLine(), 0);
									continue;
								} catch (Exception ex) {
									MiraiSongPlugin.getMLogger().warning(ex);
								}
								MiraiSongPlugin.getMLogger()
								.warning("权限配置文件" + ff.getName() + "的第" + i + "行有语法错误！");
							}
						}
						local.put(gn, bm);
					}
				}
			} catch (Exception ex) {
				MiraiSongPlugin.getMLogger().warning(ex);
				MiraiSongPlugin.getMLogger().warning("权限配置文件" + ff.getName() + "读取失败：" + ex.getMessage());
			}
		}
	}

	@Override
	public List<String> getValue() {
		return global.getValue();
	}

	public boolean loadString(String s, Group g, Bot b) {
		BotMatcher bm = local.computeIfAbsent(b.getId(), x -> new BotMatcher());
		if (bm.loadMatcher(s, g.getId())) {
			try (FileOutputStream fis = new FileOutputStream(new File(loadfrom, b.getId() + ".permission"), true);
					PrintStream sc = new PrintStream(fis)) {
				sc.println();
				sc.print(s);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return true;
		}
		return false;
	}
}
