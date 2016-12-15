public class GenerateApi {
	private static String name;
	private static String userTableName;
	private static int no;
	public static void main(String[] args) {
		if(args.length < 3)
			printUsage();
		else{
			String[] first = args[0].split("=");
			String[] second = args[1].split("=");
			String[] third = args[2].split("=");
			processArgs(first);
			processArgs(second);
			processArgs(third);
		}
		if(no > 1){
			new MultiParticipate(name,no,userTableName).generateApi();
		}
		else if(no==1){
			new SingleParticipate(name).generateApi();
		}
		else if(no < 1){
			System.out.println("No can not be zero or negative");
			printUsage();
		}
	}
	
	public static void printUsage() {
		System.out.println("Usage : ");
		System.out.println("    --name=xyz (name of the event table and file name will be same)");
		System.out.println("    --userTableName=user (name of the table in which users' email and password is stored)");
		System.out.println("    --no=3 (no of participant in this event)");
	}

	public static void processArgs(String[] args) {
		if(args.length < 2)
		{
			printUsage();
			return;
		}
		if(args[0].startsWith("--name")) {
			if(args[1].length() > 0)
				name = args[1];
			else
				printUsage();
		}
		else if(args[0].startsWith("--no")) {
			if(args[1].length () > 0)
				no = Integer.parseInt(args[1]);
			else
				printUsage();
		}
		else if(args[0].startsWith("--userTableName")) {
			if(args[1].length() > 0)
				userTableName = args[1];
			else
				printUsage();
		}
		else
			printUsage();
	}
}