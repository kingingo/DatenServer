package eu.epicpvp.teamspeak;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import eu.epicpvp.datenserver.definitions.hashmaps.CachedHashMap;
import lombok.NonNull;

public class MinecraftUtils {
	private static final Rectangle HEAD_REGION = new Rectangle(8, 8, 8, 8);
	private static final Rectangle HEAD_OVERLAY_REGION = new Rectangle(5*8, 8, 8, 8);
	private static CachedHashMap<String, BufferedImage> skins = new CachedHashMap<>(10, TimeUnit.MINUTES);

	public static BufferedImage getSkin(String name) {
		name = name.toLowerCase().intern();
		if (skins.containsKey(name) && skins.get(name) != null)
			return skins.get(name);

		URL url = null;
		try {
			url = new URL("http://skins.minecraft.net/MinecraftSkins/" + name + ".png");
		} catch (MalformedURLException e) {
			System.err.println("Unable to parse skin URL");
			e.printStackTrace();
			return null;
		}

		try {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setInstanceFollowRedirects(true);
			conn.setDoInput(true);
			conn.setDoOutput(false);
			conn.setRequestMethod("GET");
			conn.setDefaultUseCaches(false);
			conn.setUseCaches(false);
			InputStream inStream = conn.getInputStream();
			BufferedImage image = ImageIO.read(inStream);
			skins.put(name, image);
			inStream.close();
			return image;
		} catch (IOException e) {
			System.err.println("IOException while downloading skin!");
			e.printStackTrace();
		}
		return null;
	}
	
	public static BufferedImage getHead(String name, boolean overlay){
		BufferedImage skin = getSkin(name);
		if(skin == null)
			return null;
		return getHeadPart(skin, overlay);
	}
	
	@NonNull
	public static BufferedImage getHeadPart(BufferedImage skin, boolean overlay) {
		BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
		Graphics2D grafics = (Graphics2D) image.getGraphics();
		grafics.setColor(Color.RED);
		grafics.fillRect(0, 0, 8, 8);
		
		BufferedImage tempImage = skin.getSubimage((int) HEAD_REGION.getX(), (int) HEAD_REGION.getY(), (int) HEAD_REGION.getWidth(),(int) HEAD_REGION.getHeight());
		grafics.drawImage(tempImage, 0, 0, 8, 8, null);
		if(overlay){
			tempImage = skin.getSubimage((int) HEAD_OVERLAY_REGION.getX(), (int) HEAD_OVERLAY_REGION.getY(), (int) HEAD_OVERLAY_REGION.getWidth(),(int) HEAD_OVERLAY_REGION.getHeight());
			grafics.drawImage(tempImage, 0, 0, 8, 8, null);
		}
		grafics.dispose();
		return image;
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame() {
			public void paint(java.awt.Graphics g) {
				((Graphics2D)g).scale(1, 1);
				
				/*
				BufferedImage image = getSkin("WolverinDEV");
				ImageScaler scaled = new ImageScaler(image);
				//scaled.createScaledImage(image.getHeight()*30, image.getHeight()*30, ScaleType.FIT);
				BufferedImage img = toBufferedImage(scaled.getScaledImage().getImage());
				g.drawImage(img, 0, 0, this);
				g.setColor(Color.RED);
				((Graphics2D)g).drawRect((int) HEAD_REGION.getX(), (int) HEAD_REGION.getY(), (int) HEAD_REGION.getWidth(),(int) HEAD_REGION.getHeight());
				g.setColor(Color.GREEN);
				((Graphics2D)g).drawRect((int) HEAD_OVERLAY_REGION.getX(), (int) HEAD_OVERLAY_REGION.getY(), (int) HEAD_OVERLAY_REGION.getWidth(),(int) HEAD_OVERLAY_REGION.getHeight());
				*/
				Image img = getHead("Janmm14", true);
				img = img.getScaledInstance(16 * 8, 16 * 8, 1);
				g.drawImage(img, 100 - 8 * 8, 100 - 8 * 8, this);
			};
		};
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(200, 200);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public static BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bimage;
	}
}
