package org.hypher.gradientea.artnet.player;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.hypher.gradientea.geometry.shared.GeoVector3;
import org.hypher.gradientea.geometry.shared.GeodesicDomeGeometry;
import org.hypher.gradientea.geometry.shared.GeodesicDomeSpec;
import org.hypher.gradientea.transport.shared.DomeAnimationFrame;
import org.hypher.gradientea.transport.shared.DomeAnimationTransport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple HTTP server which can receive animation frames from POST requests and play them back on the dome
 * at the specified speed.
 *
 * <h2>Data Format</h2>
 * The server expects a POST of plain text where each line represents a frame. Each color value should be hex encoded
 * and zero-padded. No spacing or other characters are supported. The number of values must be divisible by three.
 *
 * <h3>Frame Rate</h3>
 * The frame rate of the data may be specified with a the GET parameter {@code fps}. The default is 30 fps. This frame
 * rate is stored globally and will affect all frames currently in the buffer.
 *
 * <h3>Response &amp; Buffering</h3>
 * The response of the call will be the estimated number of milliseconds until the current animation buffer is empty.
 * If the animation buffer is already empty, the return value will be negative and indicate when the last frame was
 * played. Clients should attempt to keep data in the buffer but not overflow the maximum of {@value #BUFFER_SIZE}
 * buffered frames.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class HttpReceiver {

	public final static int BUFFER_SIZE = 1000;

	private int fps = 30;
	private List<byte[]> buffer = Collections.<byte[]>synchronizedList(Lists.<byte[]>newLinkedList());
	private long lastFramePlayed = System.currentTimeMillis();
	private HttpServer server;

	private DomeAnimationTransport animationTransport;
	private Thread playerThread;
	private volatile long lastFrameDisplayed;


	public HttpReceiver(final DomeAnimationTransport animationTransport) {
		this.animationTransport = animationTransport;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Interface Methods

	public void start(int port) throws IOException {
		stop();

		server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext("/display", displayHandler);
		server.createContext("/mapping", mappingHandler);
		server.setExecutor(null); // creates a default executor
		server.start();

		playerThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (server != null) {
					lastFrameDisplayed = System.currentTimeMillis();

					// Send a frame
					synchronized (buffer) {
						if (! buffer.isEmpty()) {
							animationTransport.displayFrame(new DomeAnimationFrame(
								buffer.remove(0)
							));
						}
					}

					// Wait for the next frame
					try {
						Thread.sleep(Math.max(0,
							1000/fps - (System.currentTimeMillis() - lastFrameDisplayed)
						));
					} catch (InterruptedException e) {}
				}
			}
		});
		playerThread.setDaemon(true);
		playerThread.start();
	}

	public void stop() {
		if (server != null) {
			server.stop(0);
			server = null;

			playerThread = null;
		}
	}

	private HttpHandler displayHandler = new HttpHandler() {
		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			try {
				if (exchange.getRequestURI().getQuery() != null) {
					Matcher fpsMatcher = Pattern.compile("fps=(\\d+)").matcher(exchange.getRequestURI().getQuery());

					if (fpsMatcher.find()) {
						fps = Integer.parseInt(fpsMatcher.group(1));
					}
				}

				BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));

				for (String line; (line= reader.readLine()) != null;) {
					buffer.add(parseFrame(line));
				}

				byte[] response = String.valueOf((int) (buffer.size() * (1000.0 / fps))).getBytes();

				exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
				exchange.sendResponseHeaders(200, response.length);
				exchange.getResponseBody().write(response);
				exchange.getResponseBody().close();
			} catch (Exception e) {
				StringWriter writer = new StringWriter();
				e.printStackTrace();
				e.printStackTrace(new PrintWriter(writer));

				byte[] response = writer.toString().getBytes();

				exchange.sendResponseHeaders(500, response.length);
				exchange.getResponseBody().write(response);
				exchange.getResponseBody().close();
			}
		}
	};

	private HttpHandler mappingHandler = new HttpHandler() {
		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			if (exchange.getRequestURI().getQuery() != null) {
				Matcher frequencyMatcher = Pattern.compile("frequency=(\\d+)").matcher(exchange.getRequestURI().getQuery());
				Matcher layersMatcher = Pattern.compile("layers=(\\d+)").matcher(exchange.getRequestURI().getQuery());

				exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
				exchange.sendResponseHeaders(200, 0);

				PrintWriter writer = new PrintWriter(exchange.getResponseBody());
				if (frequencyMatcher.find() && layersMatcher.find()) {
					final GeodesicDomeGeometry domeGeometry = new GeodesicDomeGeometry(
						new GeodesicDomeSpec(
							Integer.valueOf(frequencyMatcher.group(1)),
							Integer.valueOf(layersMatcher.group(1))
						)
					);

					List<GeoVector3> verticies = Ordering.from(GeoVector3.xyzComparator).immutableSortedCopy(domeGeometry.getVertices());

				}

				writer.close();
			}
		}
	};

	private byte[] parseFrame(final String line) {
		byte[] data = new byte[line.length()/2];

		for (int i=0; i<data.length; i++) {
			data[i] = (byte) Integer.parseInt(line.substring(i*2,i*2+2), 16);
		}

		return data;
	}

	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Getters and Setters

	//endregion
}
