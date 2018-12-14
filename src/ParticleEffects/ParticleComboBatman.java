package ParticleEffects;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ParticleComboBatman extends ParticleComboEffect{
	ParticleEffect particle = ParticleEffect.FIREWORKS_SPARK;
	String text = "Text";
	boolean invert = false;
	int stepX = 1, stepY = 1;
	float size = (float) 1/5;
	Font font = new Font("Tahoma", Font.PLAIN, 16);
	private BufferedImage image = null;
	
	@Override
	public void display(Location loc, Player... ppl){
		int clr = 0;
		try {
			if(image == null){
				image = StringParser.stringToBufferedImage(font, text);
			}
			for (int y = 0; y < image.getHeight(); y += stepY) {
				for (int x = 0; x < image.getWidth(); x += stepX) {
					clr = image.getRGB(x, y);
					if (!invert && Color.black.getRGB() != clr) {
						continue;
					} else if (invert && Color.black.getRGB() == clr) {
						continue;
					}
					Vector v = new Vector((float) image.getWidth() / 2 - x, (float) image.getHeight() / 2 - y, 0).multiply(size);
					VectorUtils.rotateAroundAxisY(v, (180-loc.getYaw()) * MathUtils.degreesToRadians);
					particle.display(0, 0, 0, 0, 1, loc.add(v), ppl);
					loc.subtract(v);
				}
			}
		}
		catch(Exception ex){ex.printStackTrace();}
	}
}