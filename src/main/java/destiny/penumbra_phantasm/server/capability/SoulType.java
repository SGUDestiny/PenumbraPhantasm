package destiny.penumbra_phantasm.server.capability;

public enum SoulType
{
	DETERMINATION(1),
	PATIENCE(2),
	BRAVERY(3),
	INTEGRITY(4),
	PERSEVERANCE(5),
	KINDNESS(6),
	JUSTICE(7);

	final int id;
	SoulType(int id)
	{
		this.id = id;
	}

	public static SoulType byId(int id)
	{
		return switch(id)
		{
			case 7 -> JUSTICE;
			case 6 -> KINDNESS;
			case 5 -> PERSEVERANCE;
			case 4 -> INTEGRITY;
			case 3 -> BRAVERY;
			case 2 -> PATIENCE;
			case 1 -> DETERMINATION;

			default -> DETERMINATION;
		};

	}
}
