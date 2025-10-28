package com.osato.countries.services;

import com.osato.countries.models.entities.Country;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.imageio.ImageIO;

@Service
public class ImageService {

	@Value("${app.cache-dir:cache}")
	private String cacheDir;

	@PostConstruct
	public void ensureCache() {
		// ensure headless mode so Graphics works in container environments
		System.setProperty("java.awt.headless", "true");
		File d = new File(cacheDir);
		if (!d.exists()) d.mkdirs();
	}

	public void generateSummaryImage(long totalCountries, List<Country> top5, Instant timestamp) throws Exception {
		// use BufferedImage and Graphics2D only — headless will allow this in containers.
		int width = 1200, height = 600;
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();

		try {
			g.setPaint(Color.WHITE);
			g.fillRect(0, 0, width, height);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.setPaint(Color.BLACK);
			g.setFont(new Font("SansSerif", Font.BOLD, 36));
			g.drawString("Countries Summary", 40, 60);

			g.setFont(new Font("SansSerif", Font.PLAIN, 18));
			String ts = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC).format(timestamp);
			g.drawString("Last refreshed: " + ts, 40, 100);
			g.drawString("Total countries: " + totalCountries, 40, 130);

			g.setFont(new Font("SansSerif", Font.BOLD, 22));
			g.drawString("Top 5 countries by estimated GDP", 40, 180);

			g.setFont(new Font("Monospaced", Font.PLAIN, 18));
			NumberFormat nf = NumberFormat.getInstance();
			int y = 210;
			int i = 1;
			for (Country c : top5) {
				String name = c.getName() == null ? "N/A" : c.getName();
				Double gdp = c.getEstimatedGdp();
				String gdpStr = (gdp == null) ? "N/A" : nf.format(Math.round(gdp * 100.0) / 100.0);
				g.drawString(String.format("%d. %-30s %15s", i++, name, gdpStr), 40, y);
				y += 30;
			}
		} finally {
			g.dispose();
		}

		File out = new File(cacheDir, "summary.png");
		ImageIO.write(img, "png", out);
	}
}
