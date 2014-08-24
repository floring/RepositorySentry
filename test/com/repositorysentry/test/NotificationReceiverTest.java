package com.repositorysentry.test;

import com.repositorysentry.GitRepository;
import com.repositorysentry.NotificationReceiver;
import com.repositorysentry.Repository;
import com.repositorysentry.SentryCreator;

import android.content.Intent;
import android.test.AndroidTestCase;

public class NotificationReceiverTest extends AndroidTestCase {
	
	private NotificationReceiver mReceiver;
    private TestContext mContext;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mReceiver = new NotificationReceiver();
        mContext = new TestContext();
    }
    
    public void testOnReceive()
    {
    	Repository repo = new GitRepository(mContext, "tUsername", "tRepoName");
        Intent intent = new Intent();
        intent.putExtra(SentryCreator.INTENT_KEY_REPO, repo);
        
        mReceiver.onReceive(mContext, intent);        
        
    }

}
