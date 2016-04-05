package dev.wolveringer.twitter;

import twitter4j.IDs;
import twitter4j.PagableResponseList;
import twitter4j.RateLimitStatusEvent;
import twitter4j.RateLimitStatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterManager implements RateLimitStatusListener{
	private static TwitterManager manager;
	public static void setManager(TwitterManager manager) {
		TwitterManager.manager = manager;
	}
	public static TwitterManager getManager() {
		return manager;
	}
	
	String consumer_key;
	String consumer_secret;
	String token_access;
	String token_secret;
	Twitter twitter;
	ConfigurationBuilder config;
	
	public TwitterManager(String consumer_key,String consumer_secret,String token_access,String token_secret) {
		this.consumer_key = consumer_key;
		this.consumer_secret = consumer_secret;
		this.token_access = token_access;
		this.token_secret = token_secret;
		this.config = new ConfigurationBuilder().setDebugEnabled(false).setAsyncNumThreads(10).setOAuthConsumerKey(consumer_key).setOAuthConsumerSecret(consumer_secret).setOAuthAccessToken(token_access).setOAuthAccessTokenSecret(token_secret);
	}
	
	public void connect(){
		try{
			twitter = new TwitterFactory(config.build()).getInstance();
			twitter.addRateLimitStatusListener(this);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	@Override
	public void onRateLimitStatus(RateLimitStatusEvent event) {
		System.out.println("Rate limit: "+event.getRateLimitStatus().getLimit()+":"+event.getRateLimitStatus().getRemaining()+":"+event.getRateLimitStatus().getResetTimeInSeconds()+":"+event.getRateLimitStatus().getSecondsUntilReset()+":"+event.isAccountRateLimitStatus()+":"+event.isIPRateLimitStatus());
	}
	@Override
	public void onRateLimitReached(RateLimitStatusEvent event) {
		System.out.println("Rate limit reatched");
	}
	
	public void printFollowers(){
		try {
			PagableResponseList<User> users;
			long ownId = twitter.getId();
			 long cursor = -1;
	            System.out.println("Listing followers's ids.");
	            do {
	                for(User u : users = twitter.getFollowersList("EpicPvPMC",cursor, 500))
	                	System.out.println("Id: "+u.getId()+" Name:"+u.getScreenName());
	            } while ((cursor = users.getNextCursor()) != 0);
		} catch (IllegalStateException | TwitterException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		TwitterManager m = new TwitterManager("oqijDmSyaYchleoKg4BZHkgAy", "baZ1ACY5HCcB16i5IkJgImS0zsRg5EvSBRNUDSdS9nE8PSk94v", "2683690933-Q3RICLRM0NLFKJ3C38t8gQDEFyPAQDoOFhtAYTU", "FWYWzxX8p7FStWIqyDiaX7zPBEAunz1P397DpzQxymL3R");
		m.connect();
		m.printFollowers();
	}
}
