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
package com.khjxiaogu.MiraiSongPlugin.cardprovider;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import com.khjxiaogu.MiraiSongPlugin.MusicCardProvider;
import com.khjxiaogu.MiraiSongPlugin.MusicInfo;
import com.khjxiaogu.MiraiSongPlugin.Utils;

import net.mamoe.mirai.contact.AudioSupported;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

public class SilkVoiceProvider implements MusicCardProvider {
	public static String silk = "silk_v3_encoder";
	public static String ffmpeg ="ffmpeg";

	public SilkVoiceProvider() {
	}

	@Override
	public Message process(MusicInfo mi, Contact ct) {
		HttpURLConnection huc2 = null;
		try {
			huc2 = (HttpURLConnection) new URL(mi.murl).openConnection();
			if (mi.properties != null)
				for (Map.Entry<String, String> me : mi.properties.entrySet())
					huc2.addRequestProperty(me.getKey(), me.getValue());
			huc2.setRequestMethod("GET");
			huc2.connect();
		} catch (IOException e) {
			return new PlainText("获取音频失败");
		}
//		File f = new File("temp/", "wv" + System.currentTimeMillis() + ".m4a");
//		File f2 = new File("temp/", "wv" + System.currentTimeMillis() + ".silk");
//		File ft = new File("temp/", "wv" + System.currentTimeMillis() + ".pcm");
//		// File f2=new File("./temp/","wv"+System.currentTimeMillis()+".amr");
//		try {
//			f.getParentFile().mkdirs();
//			OutputStream os = new FileOutputStream(f);
//			os.write(Utils.readAll(huc2.getInputStream()));
//
//			os.close();
//			// exeCmd(new File("ffmpeg.exe").getAbsolutePath() + " -i \"" +
//			// f.getAbsolutePath()
//			// + "\" -ab 12.2k -ar 8000 -ac 1 -y " + f2.getAbsolutePath());
//			Utils.exeCmd(ffmpeg, "-i", f.getAbsolutePath(), "-f", "s16le", "-ar", "24000", "-ac", "1",
//					"-acodec", "pcm_s16le", "-y", ft.getAbsolutePath());
//			Utils.exeCmd(silk, ft.getAbsolutePath(), f2.getAbsolutePath(), "-Fs_API", "24000",
//					"-tencent");
//			try (FileInputStream fis = new FileInputStream(f2);ExternalResource ex=ExternalResource.create(fis)) {
//				return Utils.uploadVoice(ex,ct);
//			}
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} finally {
//			f.delete();
//			ft.delete();
//			f2.delete();
//		}
//		return new PlainText("当前状态不支持音频");
		try (InputStream stream = huc2.getInputStream()) { // 安全地使用 InputStream
			try (ExternalResource resource = ExternalResource.create(stream)) { // 安全地使用资源
				return ((AudioSupported) ct).uploadAudio(resource);
			} catch (Exception e) {
				return new PlainText("当前状态不支持音频");
			}
		} catch (Exception e) {
			return new PlainText("当前状态不支持音频");
		}
	}
}
