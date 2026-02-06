package destiny.penumbra_phantasm.client.render;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class TypewriterText
{
	public final Component text;
	public final int startTime;
	public int transparentTick;
	public final int ticksPerChar;

	public TypewriterText(Component text, int ticksPerChar, int startTick)
	{
		this.text = text;
		this.ticksPerChar = ticksPerChar;
		this.startTime = startTick;
		this.transparentTick = getFinishTick() + 40;
	}

	public Component getVisibleText(int tick)
	{
		long elapsed = tick - startTime;
		int chars = (int) (elapsed / ticksPerChar);
		chars = Mth.clamp(chars, 0, text.getString().length());
		return Component.translatable(text.getString(chars)).withStyle(text.getStyle());
	}

	public float getAlpha(int tick)
	{
		int finishTick = getFinishTick();
		float transparentTick = this.transparentTick;
		float transparentEnd = transparentTick + 20;


		if(tick < finishTick) return 1F;
		if(tick > transparentEnd) return 0F;

		float delta = (tick - transparentTick) / (transparentEnd - transparentTick);
		delta = Math.max(0f, Math.min(1f, delta));

		float alpha = Mth.lerp(delta, 1f, 0f);
//		System.out.println("---------------");
//		System.out.println("Tick - " + tick);
//		System.out.println("Text startTick - " + startTime);
//		System.out.println("Text transparent time - " + transparentTick);
//		System.out.println("Text finish time - " + finishTick);
//		System.out.println("Text transparency - " + alpha);
		return alpha;
	}

	public int getFinishTick()
	{
		return startTime + (text.getString().length() * ticksPerChar);
	}

	public TypewriterText syncTransparency(TypewriterText other)
	{
		other.transparentTick = this.transparentTick;
		return this;
	}

	public boolean isFinished(int tick)
	{
		return getVisibleText(tick).getString().length() >= text.getString().length();
	}
}
