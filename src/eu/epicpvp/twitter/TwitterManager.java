package eu.epicpvp.twitter;

import lombok.Getter;
import lombok.Setter;
import twitter4j.RateLimitStatusEvent;
import twitter4j.RateLimitStatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterManager implements RateLimitStatusListener{
	@Setter
	@Getter
	public static TwitterManager twitterManager;
	
	private Twitter twitter;
	private ConfigurationBuilder config;
	
	public TwitterManager(String consumer_key,String consumer_secret,String token_access,String token_secret) {
		this.config = new ConfigurationBuilder().setDebugEnabled(false).setAsyncNumThreads(10).setOAuthConsumerKey(consumer_key).setOAuthConsumerSecret(consumer_secret).setOAuthAccessToken(token_access).setOAuthAccessTokenSecret(token_secret);
		connect();
	}
	
	public void connect(){
		try{
			this.twitter = new TwitterFactory(config.build()).getInstance();
			this.twitter.addRateLimitStatusListener(this);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public long getTwitterID(String username){
		try {
			User user = this.twitter.showUser(username);
			return user.getId();
		} catch (TwitterException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public boolean isFollower(String username){
		try {
			User user = this.twitter.showUser(username);
			return this.twitter.showFriendship(user.getId(), this.twitter.getId()).isSourceFollowingTarget();
		} catch (TwitterException e) {
			e.printStackTrace();
			return false;
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
}