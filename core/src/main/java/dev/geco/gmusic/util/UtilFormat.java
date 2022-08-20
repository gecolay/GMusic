package dev.geco.gmusic.util;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.*;
import java.util.regex.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class UtilFormat {
	
	public List<String> sortList(List<String> List) {
		final Pattern intsOnly = Pattern.compile("\\d+");
		Comparator<String> comparator = new Comparator<String>() {
		    @Override
		    public int compare(final String string1, final String string2) {
		        String int1 = null;
		        String int2 = null;
		        Matcher matcher1 = intsOnly.matcher(string1);
		        if (matcher1.find()) {
		            int1 = matcher1.group();
		            Matcher matcher2 = intsOnly.matcher(string2);
		            if(matcher2.find()) int2 = matcher2.group();
		        }
		        int result = 0;
		        if(int1 != null && int2 != null) result = Integer.valueOf(int1).compareTo(Integer.valueOf(int2));
		        return result == 0 ? string1.compareTo(string2) : result;
		    }
		};
		Collections.sort(List, comparator);
		return List;
	}
	
	public String getLocationString(Location L) { return L.getWorld().getName() + ":" + L.getBlockX() + ":" + L.getBlockY() + ":" + L.getBlockZ(); }
	
	public Location getStringLocation(String L) {
		String[] a = L.split(":");
		return new Location(Bukkit.getWorld(a[0]), Double.parseDouble(a[1]), Double.parseDouble(a[2]), Double.parseDouble(a[3]));
	}
	
	public String convertTime(long Mil) {
		long se = Mil / 1000;
		long s = se % 60;
		long m = se / 60;
		if(m >= 60) {
			long h = m / 60;
			m %= 60;
			return (h < 10 ? "0" + h : h) + ":" + (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + s : s);
		}
		return (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + s : s);
	}
	
	public void downloadFile(String Url, File F) throws Exception {
		FileOutputStream fos = new FileOutputStream(F);
		fos.getChannel().transferFrom(Channels.newChannel(new URL(Url).openStream()), 0, Long.MAX_VALUE);
		fos.close();
	}
	
	public class UtilMath {
		
		private double[] cos = new double[360];
		private double[] sin = new double[360];
		
		public UtilMath() {
			for(int d = 0; d < 360; d++) {
			    cos[d] = Math.cos(Math.toRadians(d));
			    sin[d] = Math.sin(Math.toRadians(d));
			}
		}
		
		public Location convertToStero(Location Location, float Shift) {
			float y = Location.getYaw();
			return Location.clone().add(cos[(int) (y + 360) % 360] * Shift, 0, sin[(int) (y + 360) % 360] * Shift);
		}
		
	}
	
}