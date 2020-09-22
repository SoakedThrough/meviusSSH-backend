package com.meviusssh.backend.utils;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelMatcher;


public class MeviusChannelMatcher implements ChannelMatcher {

    public MeviusChannelMatcher(String theChannel) {
        this.theChannel = theChannel;
    }

    public String getTheChannel() {
        return theChannel;
    }

    public void setTheChannel(String theChannel) {
        this.theChannel = theChannel;
    }

    public  String theChannel;

    @Override
    public boolean matches(Channel channel) {
        if (channel.id().asShortText().equals(theChannel)){
            return true;
        }
        return false;
    }
}
