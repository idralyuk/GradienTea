package org.hypher.gradientea.artnet.player.animations;

import org.hypher.gradientea.artnet.player.UdpDomeClient;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeGeometry;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeSpecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class StaticImageMapper implements Runnable {
	public static void main(String[] args) throws IOException {
		new StaticImageMapper();
	}
	private UdpDomeClient transport = new UdpDomeClient();

	private GradienTeaDomeGeometry geometry = new GradienTeaDomeGeometry(GradienTeaDomeSpecs.GRADIENTEA_DOME);
	private DomeImageMapper mapper = new DomeImageMapper(geometry);

	private File file = new File("/Users/yona/devel/personal/GradienTea-stable/java/ArtNetPlayer/src/main/resources/testImage3.jpg");
	private BufferedImage image;
	private long lastImageUpdate;

	public StaticImageMapper() throws IOException {
		transport.connect("localhost");

		new Thread(this).start();
	}


	@Override
	public void run() {
		DomePixelCanvas canvas = new DomePixelCanvas(
			org.hypher.gradientea.artnet.player.linear.animations.canvas.PixelCompositor.ADDITIVE,
			geometry);

		while (true) {
			long start = System.currentTimeMillis();

			canvas.clear();
			draw(canvas);

			transport.displayFrame(canvas.render());

			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {}
		}
	}

	private void draw(final DomePixelCanvas canvas) {
		try {
			if (lastImageUpdate != file.lastModified()) {
				image = ImageIO.read(file);
				lastImageUpdate = file.lastModified();
			}
		} catch (IOException e) {}

		long start = System.currentTimeMillis();
		mapper.drawImage(
			image,
			canvas
		);
		System.out.println("Time: " + (System.currentTimeMillis() - start));
	}
}
