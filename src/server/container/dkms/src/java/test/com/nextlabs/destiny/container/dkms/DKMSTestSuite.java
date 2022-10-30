package com.nextlabs.destiny.container.dkms;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.nextlabs.destiny.container.dkms.impl.KeyIdTest;
import com.nextlabs.destiny.container.dkms.impl.KeyRingManagerTest;
import com.nextlabs.destiny.container.dkms.impl.KeyRingTest;
import com.nextlabs.destiny.container.dkms.impl.KeyTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    KeyManagementRepositoryTest.class
  , KeyIdTest.class
  , KeyRingManagerTest.class
  , KeyRingTest.class
  , KeyTest.class
})
public class DKMSTestSuite {

}
