package com.bluejungle.pf.destiny.services;

/**
 * Allow the other class call client.preapreForTests().
 * such for importexport unit test
 *
 * @author hchan
 * @date May 2, 2007
 */
public class PolicyEditorClientMock{
	public static void prepareForTest(PolicyEditorClient client) throws PolicyEditorException{
		client.prepareForTests();
	}
}
