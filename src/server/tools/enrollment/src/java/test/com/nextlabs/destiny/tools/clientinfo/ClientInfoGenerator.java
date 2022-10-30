/*
 * Created on Mar 29, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.tools.clientinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import static com.nextlabs.destiny.tools.clientinfo.ClientInfoGeneratorOptionDescriptorEnum.*;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.RetrievalFailedException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.dirsync.DirSyncControl;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.dirsync.DirSyncDispatcher;
import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.bluejungle.framework.utils.CollectionUtils;
import com.nextlabs.destiny.container.shared.dictionary.enrollment.enroller.clientinfo.ClientInfoTag;
import com.nextlabs.random.Dictionary;
import com.nextlabs.random.RandomString;
import com.nextlabs.random.RandomWithAverage;
import com.nextlabs.shared.tools.ConsoleApplicationBase;
import com.nextlabs.shared.tools.ICommandLine;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.impl.InteractiveQuestion;
import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollment/src/java/test/com/nextlabs/destiny/tools/clientinfo/ClientInfoGenerator.java#1 $
 */

public class ClientInfoGenerator extends ConsoleApplicationBase{
	
	private static final int MIN_SHARE_DOMAIN = 2 ;
	private static final int MAX_SHARE_DOMAIN = 8 ;
	
	static final String SCOPE_SUB_VALUE = "SUB";
	static final Map<String, Integer> SCOPE_TO_INTEGER_MAP;
	static{
		SCOPE_TO_INTEGER_MAP = new HashMap<String, Integer>();
		SCOPE_TO_INTEGER_MAP.put("BASE", LDAPConnection.SCOPE_BASE);
		SCOPE_TO_INTEGER_MAP.put("ONE", LDAPConnection.SCOPE_ONE);
		SCOPE_TO_INTEGER_MAP.put(SCOPE_SUB_VALUE, LDAPConnection.SCOPE_SUB);
	}
	
	private static final ReversibleEncryptor CIPHER = new ReversibleEncryptor();
	
	private static final String[] DOMAIN_NAME_SUFFIX = new String[]{
		"com", "net", "org", "tv", "info", "mobi", "me", "biz", "us", "name",
		};
	
	private final IConsoleApplicationDescriptor consoleApplicationDescriptor;
	
	
	public ClientInfoGenerator() throws InvalidOptionDescriptorException {
		consoleApplicationDescriptor = new ClientInfoGeneratorOptionDescriptorEnum();
	}


	public static void main(String[] args){
		try{
			ClientInfoGenerator mgr = new ClientInfoGenerator();
			mgr.parseAndExecute(args);
		}catch(Exception e){
			printException(e);
		}	
	}
	
	/**
	 * @see com.nextlabs.shared.tools.ConsoleApplicationBase#execute(com.nextlabs.shared.tools.ICommandLine)
	 */
	@Override
	protected void execute(ICommandLine commandLine) {
		String createType = getValue(commandLine, CREATE_OPTION_ID);
		
		try {
			if(createType.equals(CREATE_USERS_VALUE)){
				createUsersFile(commandLine);
			}else if(createType.equals(CREATE_DOMAINS_VALUE)){
				createDomainsFile(commandLine);
			}else if(createType.equals(CREATE_CLIENT_INFO_VALUE)){
				createClientInfo(commandLine);
			}else{
				throw new IllegalArgumentException("createType is unknown: " + createType);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void createClientInfo(ICommandLine commandLine) throws IOException {
		final int usersMin = getValue(commandLine, USER_NUM_MIN_OPTION_ID);
		final int usersMax = getValue(commandLine, USER_NUM_MAX_OPTION_ID);
		final int usersAve = getValue(commandLine, USER_NUM_AVERAGE_OPTION_ID);
		final int domainsMin = getValue(commandLine, DOMAIN_NUM_MIN_OPTION_ID);
		final int domainsMax = getValue(commandLine, DOMAIN_NUM_MAX_OPTION_ID);
		final int domainsAve = getValue(commandLine, DOMAIN_NUM_AVERAGE_OPTION_ID);

		final int clientNumber = getValue(commandLine, CLIENT_NUM_OPTION_ID);

		final String outputFileStr = getValue(commandLine, OUTPUT_FILE_OPTION_ID);
		final File outputFile = new File(outputFileStr);
		final String userFileStr = getValue(commandLine, USER_FILE_OPTION_ID);

		final String domainFileStr = getValue(commandLine, DOMAIN_FILE_OPTION_ID);
		
		final float dupDomainPercentage = getValue(commandLine, DUPLICATE_DOMAIN_PERCENTAGE_OPTION_ID);
		
		
		final List<String> domains = loadFromFile(domainFileStr);
		final List<String> users = loadFromFile(userFileStr);
		
		Dictionary dictionary = new Dictionary();
		
		if (domainsMax > domains.size()) {
			System.err.println("The requested domain size " + domainsMax
					+ "  is larger than possible size which is " + domains.size());
			return;
		}
		
		final boolean hasWarning; 
		if(clientNumber * domainsAve > domains.size()){
			System.err.println("There is a very high chance that a domain will be duplicated between different clients.");
			hasWarning = true;
		}else if(clientNumber * domainsMax > domains.size()){
			System.err.println("There is a chance that a domain will be duplicated between different clients.");
			hasWarning = true;
		}else{
			hasWarning = false;
		}
		
		if(hasWarning){
			String answer = InteractiveQuestion.prompt("Do you want to continue? (Y/N)");
			if (!answer.trim().equalsIgnoreCase("Y")) {
				System.out.println("Abort!");
				return;
			}
		}
		
		if (usersMax > users.size()) {
			System.err.println("The requested user size " + usersMax
					+ "  is larger than possible size which is " + users.size());
			return;
		}
		
		FileWriter fileWriter = null;
		
		final RandomWithAverage randomForDomain = new RandomWithAverage(domainsMin,
				domainsMax, domainsAve);
		final RandomWithAverage randomForUser = new RandomWithAverage(usersMin, usersMax,
				usersAve);
		Set<Integer> listed = new HashSet<Integer>();
		Set<String> generatedIds = new HashSet<String>();
		
		
		int[][] domainIndexes = createDomainIndexes(domains, clientNumber, dupDomainPercentage, randomForDomain);
		
		try {
			fileWriter = new FileWriter(outputFile, false);
			
			fileWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			fileWriter.write("<"+ ClientInfoTag.TAG_CLIENT_INFORMATION + ">\n");
			
			for (int clientIndex = 0; clientIndex < clientNumber; clientIndex++) {
				fileWriter.write("\t<" + ClientInfoTag.TAG_CLIENT + "\n");
				String id;
				do {
					id = generateRandomStr(3 + r.nextInt(8), RandomString.ALPHA + RandomString.DIGIT);
				} while (generatedIds.contains(id));
				generatedIds.add(id);
				fileWriter.write("\t\t" + ClientInfoTag.ATTR_IDENTIFIER + "=\""
						+ id + "\"\n");
				
				fileWriter.write("\t\t" + ClientInfoTag.ATTR_SHORT_NAME + "=\""
						+ generateRandomName(dictionary, 1 + r.nextInt(3)) + "\"\n");
				
				fileWriter.write("\t\t" + ClientInfoTag.ATTR_LONG_NAME + "=\""
						+ generateRandomName(dictionary, 2 + r.nextInt(5)) + "\"\n");
				fileWriter.write("\t>\n");
				
				for (int domainIndex = 0; domainIndex < domainIndexes[clientIndex].length; domainIndex++) {
					
					fileWriter.write("\t\t<" + ClientInfoTag.TAG_EMAIL_TEMPLATE + " "
							+ ClientInfoTag.ATTR_EMAIL_TEMPLATE_NAME + "=\""
							+ domains.get(domainIndexes[clientIndex][domainIndex]) + "\"/>\n");
				}
				
				
				int numOfUsers = randomForUser.next();
				listed.clear();
				for (int userIndex = 0; userIndex < numOfUsers; userIndex++) {
					int userRandomPick;
					do{
						userRandomPick = r.nextInt(users.size());
					}while(listed.contains(userRandomPick));
					listed.add(userRandomPick);
					
					fileWriter.write("\t\t<" + ClientInfoTag.TAG_USER + " "
							+ ClientInfoTag.ATTR_USER_NAME + "=\""
							+ users.get(userRandomPick) + "\"/>\n");
				}
				fileWriter.write("\t</" + ClientInfoTag.TAG_CLIENT + ">\n");
			}
			
			fileWriter.write("</" + ClientInfoTag.TAG_CLIENT_INFORMATION + ">\n");
			fileWriter.flush();
			
			System.out.println("Average number of domains: " + randomForDomain.getAverage());
			System.out.println("Average number of usrs: " + randomForUser.getAverage());
			System.out.println("Successfully output to " + outputFile.getAbsolutePath());
		} finally {
			if (fileWriter != null) {
				fileWriter.close();
			}
		}
	}
	
	private int[][] createDomainIndexes(List<String> domains, int numberOfClient,
			float expectedDuplicateDomainPercentage, RandomWithAverage randomForDomain) throws IOException {
		boolean isWarned = false;
		
		List<Integer> availabledomainIndexes = createIndexes(domains.size());
		
		int[][] clientDomainIndexesTable = new int[numberOfClient][];
		
		int numOfTotalDomain = 0;
		
		for (int curClientIndex = 0; curClientIndex < clientDomainIndexesTable.length; curClientIndex++) {
			clientDomainIndexesTable[curClientIndex] = new int[randomForDomain.next()];
			for (int curDomainIndex = 0; curDomainIndex < clientDomainIndexesTable[curClientIndex].length; curDomainIndex++) {
				if (availabledomainIndexes.size() == 0) {
					if (!isWarned) {
						System.err.println("Some clients (since ordinal number for "
								+ (curClientIndex + 1) + ") will have duplicatd domains");
						isWarned = true;
					}
					//reload
					availabledomainIndexes = createIndexes(domains.size());
				}
				int randomDomainIndex = 
					availabledomainIndexes.remove(r.nextInt(availabledomainIndexes.size()));
				clientDomainIndexesTable[curClientIndex][curDomainIndex] = randomDomainIndex;
			}
			numOfTotalDomain += clientDomainIndexesTable[curClientIndex].length;
		}
		
		if(isWarned){
			System.out.println();
			String answer = InteractiveQuestion.prompt("Since Some client al;ready have duplicated domains, " +
					"the specified percentage of duplicated domain will be ignored. Do you want to continue? (Y/N) ");
			boolean isContinure = answer.trim().equalsIgnoreCase("Y");
			if (!isContinure) {
				System.out.println("Abort by the user");
			}
			
			return clientDomainIndexesTable;
		}
		
		int expectedDuplicateDomain = (int)(expectedDuplicateDomainPercentage / 100 * numOfTotalDomain);
		
//		if(DEBUG_MODE){
//			System.out.println("expectedDuplicateDomainPercentage = " + expectedDuplicateDomainPercentage);
//			System.out.println("before");
//			displayStat(domains, clientDomainIndexesTable);
//			System.out.println();
//		}
		
		Set<Integer> luckyDomains = new HashSet<Integer>();
		for (int i = 0; i < expectedDuplicateDomain; ) {
			int luckyClient = r.nextInt(clientDomainIndexesTable.length);
			
			
			if(clientDomainIndexesTable[luckyClient].length == 0){
				continue;
			}
			
			int luckyDomain = clientDomainIndexesTable[luckyClient][r
					.nextInt(clientDomainIndexesTable[luckyClient].length)];

			if(luckyDomains.contains(luckyDomain)){
				continue;
			}
			luckyDomains.add(luckyDomain);
			
			int numOfClientWillShare = r.nextInt(MAX_SHARE_DOMAIN - MIN_SHARE_DOMAIN)
					+ MIN_SHARE_DOMAIN;

			List<Integer> theOtherLuckyClients = new ArrayList<Integer>();
			theOtherLuckyClients.add(luckyClient);
			for (int j = 0; j < numOfClientWillShare; j++) {
				int theOtherLuckyClient = r.nextInt(clientDomainIndexesTable.length);
				
				if (theOtherLuckyClients.contains(theOtherLuckyClient)
						|| clientDomainIndexesTable[theOtherLuckyClient].length == 0) {
					numOfClientWillShare--;
					continue;
				}
				theOtherLuckyClients.add(theOtherLuckyClient);
				
				clientDomainIndexesTable[theOtherLuckyClient][r
						.nextInt(clientDomainIndexesTable[theOtherLuckyClient].length)] = luckyDomain;
			}
			
			i++;
		}
		
		displayStat(domains, clientDomainIndexesTable);
		System.out.println();
		
		return clientDomainIndexesTable;
	}


	private void displayStat(List<String> domains,	int[][] clientDomainIndexesTable) {
		int[] domainStats = new int[domains.size()];
		for (int curClientIndex = 0; curClientIndex < clientDomainIndexesTable.length; curClientIndex++) {
			for (int curDomainIndex = 0; curDomainIndex < clientDomainIndexesTable[curClientIndex].length; curDomainIndex++) {
				domainStats[clientDomainIndexesTable[curClientIndex][curDomainIndex]]++;
			}
		}

		Map<Integer, Integer> domainStatResult = new TreeMap<Integer, Integer>();
		for( int sta : domainStats){
			Integer count = domainStatResult.get(sta);
			domainStatResult.put(sta, count == null ? 1 : count+1);
		}
		
		//print statistics for domain indexes table
		for(Map.Entry<Integer, Integer> entry : domainStatResult.entrySet()){
			System.out.println(entry.getValue()+ " domain(s) occures " + entry.getKey() + " times" );
		}
		
		int[] clientStats = new int[clientDomainIndexesTable.length];
		for (int currentclientIndex = 0; currentclientIndex < clientDomainIndexesTable.length; currentclientIndex++) {
			for(int currentDomainIndex = 0; 
				currentDomainIndex < clientDomainIndexesTable[currentclientIndex].length; 
				currentDomainIndex++){
				if (domainStats[clientDomainIndexesTable[currentclientIndex][currentDomainIndex]] > 1) {
					clientStats[currentclientIndex]++;
				}
			}
		}
		
		Map<Integer, Integer> clientStatResult = new TreeMap<Integer, Integer>();
		for( int sta : clientStats){
			Integer count = clientStatResult.get(sta);
			clientStatResult.put(sta, count == null ? 1 : count+1);
		}
		
		int numberOfClientsHasDuplicatedDomains =0;
		//print statistics for domain indexes table
		for(Map.Entry<Integer, Integer> entry : clientStatResult.entrySet()){
			if(entry.getKey() > 0 ){
				numberOfClientsHasDuplicatedDomains += entry.getValue();
			}
			
			System.out.println(entry.getValue() + " client(s) have " + entry.getKey()
					+ " duplicated domain(s)");
		}
		
		System.out.println("numberOfClientsHasDuplicatedDomains = " + numberOfClientsHasDuplicatedDomains);
	}
	
	private List<Integer> createIndexes(int size) {
		List<Integer> indexes = new ArrayList<Integer>(size);
		for (int i = 0; i < size; i++) {
			indexes.add(i);
		}
		return indexes;
	}

	
	private final Random r = new Random();

	private String generateRandomStr(int length, String validChars) {
		return generateRandomStr(length, validChars, validChars);
	}
	
	private String generateRandomStr(int length, String validChars, String startEndValidChars) {
		return generateRandomStr(length, validChars, startEndValidChars,startEndValidChars);
	}
	
	private String generateRandomStr(int length, String validChars, String startValidChars,
			String endValidChars) {
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			if (i == 0) {
				sb.append(startValidChars.charAt(r.nextInt(startValidChars.length())));
			} else if (i == length - 1) {
				sb.append(endValidChars.charAt(r.nextInt(endValidChars.length())));
			} else {
				sb.append(validChars.charAt(r.nextInt(validChars.length())));
			}
		}

		return sb.toString();
	}
	
	private String generateRandomName(Dictionary dictionary, int words){
	    return dictionary.gernerateRandomString(0, words, " ", false);
	}


	private void createDomainsFile(ICommandLine commandLine) throws IOException {
		final String outputFile = getValue(commandLine, OUTPUT_FILE_OPTION_ID);

		String answer;
		answer = InteractiveQuestion.prompt("Generate from dictionary? (Y/N)");
		boolean isGenearteFromDictionary = answer.trim().equalsIgnoreCase("Y");
		
		float wildcardPercentage = getNumberInRange(
				"wild card e-mail templates percentage (0-100) ? ", 0, 100) / 100.0F;
		int numberOfDomainHasWildcard = 0;
		
		final String WILDCARD_PREFIX = "*@";
		
		final int number = getPostiveNumber("Number of domain?");
		
		Set<String> domainNames = new HashSet<String>(number); 
		
		System.out.println("wildcardPercentage = " + wildcardPercentage);
		
		final int domainNameLengthLimit = 63;
		
		if(isGenearteFromDictionary){
		    Dictionary dictionary = new Dictionary();
			
			System.out.println("generate domains, length <= " + domainNameLengthLimit);
			
			
			int generatedTarget = number / 20;
			RandomWithAverage domainNameRandom = new RandomWithAverage(1,3,1);
			
			while(domainNames.size() < number){
				StringBuilder sb = new StringBuilder();
				
				boolean isWildCard = wildcardPercentage == 100 || r.nextFloat() < wildcardPercentage;
				if (isWildCard) {
					//wildcard
					sb.append(WILDCARD_PREFIX);
				}else{
					//nowildcard
					// donothing
				}
				
				
				int words = domainNameRandom.next();
				for (int j = 0; j < words; j++) {
					sb.append(dictionary.getRandomWord());
					sb.append(".");
				}
				
				sb.append(DOMAIN_NAME_SUFFIX[(r.nextInt(DOMAIN_NAME_SUFFIX.length))]);

				if(sb.length() <= domainNameLengthLimit && Pattern.matches("(\\w|\\*|\\.|-|@)*", sb.toString())){
					domainNames.add(sb.toString());
					if(isWildCard){
						numberOfDomainHasWildcard++;
					}
				}
				
				if (domainNames.size() == generatedTarget) {
					System.out.println("\r" + "generated " + generatedTarget);
					generatedTarget += number / 20;
				}
			}
		}else{
			final int minLength = getPostiveNumber("Minimum length of domains?");
			final int maxLength = getPostiveNumber("Maximum length of domains?");
			if (maxLength > domainNameLengthLimit) {
				answer = InteractiveQuestion.prompt("Warning: Domain must be " + domainNameLengthLimit +
						" characters or less. Say (y) to continue or anything else to cancel");
				if(!answer.equalsIgnoreCase("Y")){
					System.out.println("Cancel by the user.");
					return;
				}
			}
			final int averageLength = getPostiveNumber("Average length of domains?");
			
			RandomWithAverage ra = new RandomWithAverage(minLength, maxLength, averageLength);
			
			DomainNameGenerator domainNameGenerator = new DomainNameGenerator();
			for (int i = 0; i < number; i++) {
				int length = ra.next();
				String domainName = domainNameGenerator.generateDomain(length);
				if (wildcardPercentage == 100 || r.nextFloat() < wildcardPercentage) {
					//wildcard
					numberOfDomainHasWildcard++;
					
					int chopIndex = Math.min(domainName.length() - 1, WILDCARD_PREFIX.length());
					domainName = WILDCARD_PREFIX + domainName.substring(chopIndex);
				}else{
					//nowildcard
					// donothing
				}
				
				domainNames.add(domainName);
			}
			System.out.println("Average number of domain length: " + ra.getAverage());
		}
		
		System.out.println("Number of wildcard: " + numberOfDomainHasWildcard + "/"
				+ number + ", (" + +(numberOfDomainHasWildcard * 100.0 / number) + "%)");
		
		writeToFile(new File(outputFile), domainNames);
	}
	
	private class DomainNameGenerator{
		//Labels must be 63 characters or less.
		final Random r;
		
		public DomainNameGenerator() {
			r = new Random();
		}


		// <domain> ::= <subdomain> | " 
		public String generateDomain(int length){
			if(length <= 0){
				throw new IllegalArgumentException();
			}
			
			StringBuilder sb = new StringBuilder();
			
//			if( r.nextDouble() < 0.05 ){
//				return " ";
//			}else{
				generateSubDomain(length, sb);
				return sb.toString();
//			}
			
		}
		
		// <subdomain> ::= <label> | <subdomain> "." <label>
		protected void generateSubDomain(int length, StringBuilder sb) {
			if(length <= 0){
				throw new IllegalArgumentException();
			}
			
			if (length <= 2 || r.nextDouble() < 0.33) {
				generateLabel(length,sb);
			} else {
				int splitAt = r.nextInt(length - 2) + 1;
				generateSubDomain(splitAt, sb);
				sb.append(".");
				generateLabel(length - splitAt -1 , sb);
			}
		}
		
		// <label> ::= <letter> [ [ <ldh-str> ] <let-dig> ]
		protected void generateLabel(int length, StringBuilder sb){
			if(length <= 0){
				throw new IllegalArgumentException();
			}
			
			sb.append(RandomString.ALPHA.charAt(r.nextInt(RandomString.ALPHA.length())));
			if(length > 1){
				if(length > 2){
					generateLdhStr(length - 2, sb);
				}
				generateLetDig(sb);
			}
			
		}
		
		// <ldh-str> ::= <let-dig-hyp> | <let-dig-hyp> <ldh-str>
		protected void generateLdhStr(int length, StringBuilder sb) {
			if (length <= 0) {
				throw new IllegalArgumentException();
			}

			for (int i = 0; i < length; i++) {
				generateLetDigHyp(sb);
			}
		}
		
		//<let-dig-hyp> ::= <let-dig> | "-"
		protected void generateLetDigHyp(StringBuilder sb) {
			if (r.nextDouble() < 0.02) {
				sb.append("-");
			} else {
				generateLetDig(sb);
			}
		}
		
		//<let-dig> ::= <letter> | <digit>
		protected void generateLetDig(StringBuilder sb) {
			final String validChars = RandomString.ALPHA + RandomString.DIGIT;
			sb.append(validChars.charAt(r.nextInt(validChars.length())));
		}
		
		
		
	}
	
	private int getPostiveNumber(String question) throws IOException {
		int number;
		while (true) {
			try {
				String numberStr = InteractiveQuestion.prompt(question);
				number = Integer.parseInt(numberStr);
				if(number > 0){
					return number;
				}else{
					System.out.println("Input must be larger than zero");	
				}
			} catch (NumberFormatException e) {
				System.out.println("Input is not an integer");
			}
		}
	}
	
	private float getNumberInRange(String question, float lower, float upper) throws IOException {
		while (true) {
			try {
				String numberStr = InteractiveQuestion.prompt(question);
				float number = Float.parseFloat(numberStr);
				if(lower <= number && number <= upper){
					return number;
				}else{
					System.out.println("Input must be between " + lower + " and " + upper);	
				}
			} catch (NumberFormatException e) {
				System.out.println("Input is not an integer");
			}
		}
	}


	private void createUsersFile(ICommandLine commandLine) throws IOException, LDAPException,
			RetrievalFailedException {
		final String host = getValue(commandLine, HOST_OPTION_ID);
		final int port = getValue(commandLine, PORT_OPTION_ID);
		final String dn = getValue(commandLine, USER_ID_OPTION_ID);
		final String passwd = getValue(commandLine, PASSWORD_OPTION_ID);

		final String base = getValue(commandLine, BASE_OPTION_ID);
		final String filter = getValue(commandLine, FILTER_OPTION_ID);

		final List<String> attrs = getValues(commandLine, ATTRS_OPTION_ID);
//		final String scopeStr = (String) getValue(commandLine,
//				ClientInfoGeneratorOptionDescriptorEnum.SCOPE_OPTION_ID);
//		final int scope = scopeToIntegerMap.get(scopeStr);
		final String userFileStr = getValue(commandLine, OUTPUT_FILE_OPTION_ID);
		final File userFile = new File(userFileStr);
		
//		LDAPConnection connection = null;
//		connection = new LDAPConnection();
		
		System.out.println("Connect to " + host + ", port: " + port);
//		connection.connect(host, port);
		System.out.println("Login as " + dn);
//		connection.bind(3, dn, passwd.getBytes());

		System.out.println("Search " + base);
//		System.out.println("\tscope: " + scopeStr);
		System.out.println("\tfilter: " + filter);
		System.out.println("\tattrs: " + CollectionUtils.asString(attrs, ","));
		
			
//			LDAPSearchConstraints constraints = new LDAPSearchConstraints();
//	    	
//	        SimplePagedResultControl pageControl = null; 
//	        pageControl = new SimplePagedResultControl(5000, null);
//	        constraints.setControls(pageControl);
//	        constraints.setMaxResults(0);
//	        constraints.setTimeLimit(0);
//	        constraints.setServerTimeLimit(0);           
//	        constraints.setReferralFollowing(true);
	
	        
		DirSyncControl control = null; //new DirSyncControl();
		
        DirSyncDispatcher dispatcher = new DirSyncDispatcher(
                control, // DirSyncControl control, 
                host, // String server, 
                port, // int port, 
                dn, // String login, 
                CIPHER.encrypt(passwd), // String password, 
				new String[] { base }, // String roots[], 
				attrs.toArray(new String[attrs.size()]), // String[] attributesToRetrieve, 
				filter, // String filter,
				true, // boolean isPagingEnabled,
				1000); // int batchSize
        
        dispatcher.pull();
        
        List<String> usernames = new ArrayList<String>();
        while (dispatcher.hasMore()) {
            LDAPEntry ldapEntry = dispatcher.next();
            LDAPAttribute a = ldapEntry.getAttribute("userPrincipalName");
			if(a!=null){
				usernames.add(a.getStringValue());
			}else{
				System.err.println("Unnown user: " + ldapEntry.getDN());
			}
        }
        
//		List<String> usernames = new ArrayList<String>();
//		while (results.hasMore()) {
//			LDAPEntry ldapEntry = results.next();
//			LDAPAttribute a = ldapEntry.getAttribute("userPrincipalName");
//			if(a!=null){
//				usernames.add(a.getStringValue());
//			}else{
//				System.err.println("Unnown user: " + ldapEntry.getDN());
//			}
//		}
		System.out.println("find " + usernames.size() + " results");

		writeToFile(userFile, usernames);
		
	}


	private void writeToFile(final File file, Collection<String> list) throws IOException {
		boolean isAppend;
		if(file.exists()){
			String answer = InteractiveQuestion.prompt("File " + file.getAbsolutePath() + 
					" already exists. " +
					"Do you want to append(A) the content, overwrite(O) the content or cancel(C).");
			answer = answer.toUpperCase();
			while(!answer.equals("A") && !answer.equals("O") && !answer.equals("C") ){
				answer = InteractiveQuestion.prompt("A/O/C").toUpperCase();
			}
			if(answer.equals("C")){
				System.out.println("Cancel");
				return;
			}else if(answer.equals("A")) {
				isAppend = true;
			}else if(answer.equals("O")){
				file.delete();
				isAppend = false;
			}else{
				throw new IllegalArgumentException("unknown response " + answer);
			}
			
		}else{
			isAppend = false;
		}
		
		FileWriter fileWriter = null;
		int line = 0;
		try {
			fileWriter = new FileWriter(file, isAppend);
			if(isAppend){
				System.out.println("Append entries to " + file.getAbsolutePath());
				fileWriter.append("\n");
				for(String s : list){
					fileWriter.append(s).append('\n');
					if(++line == 100){
						System.out.print(".");
					}
				}
			}else{
				for(String s : list){
					fileWriter.write(s + '\n');
					if(++line == 100){
						System.out.print(".");
					}
				}
			}
			System.out.println();
			fileWriter.flush();
			System.out.println("Successfully output to " + file.getAbsolutePath());
		} finally {
			if (fileWriter != null) {
				fileWriter.close();
			}
		}
	}
	
	private List<String> loadFromFile(String file) throws IOException {
		return loadFromFile(new FileReader(file));
	}
	
	private List<String> loadFromFile(Reader reader) throws IOException {
		List<String> list = new ArrayList<String>();
		BufferedReader bf = null;
		try {
			bf = new BufferedReader(reader);
			
			String line;
			while((line = bf.readLine()) != null){
				list.add(line);
			}
		} finally {
			if (bf != null) {
				bf.close();
			}
		}
		return list;
	}

	/**
	 * @see com.nextlabs.shared.tools.ConsoleApplicationBase#getDescriptor()
	 */
	@Override
	protected IConsoleApplicationDescriptor getDescriptor() {
		return consoleApplicationDescriptor;
	}
}
