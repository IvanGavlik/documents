@org.hibernate.annotations.FetchProfiles({
    /* 
        Each profile has a name, this is a simple string we have isolated in a constant.
     */
    @FetchProfile(name = Item.PROFILE_JOIN_SELLER,
       /* 
            Each override in a profile names one entity association or collection.
        */
        fetchOverrides = @FetchProfile.FetchOverride(
          /* 
                The only supported mode at the time of writing is <code>JOIN</code>.
           */
            entity = Item.class,
            association = "seller",
            mode = FetchMode.JOIN
        )),

    @FetchProfile(name = Item.PROFILE_JOIN_BIDS,
        fetchOverrides = @FetchProfile.FetchOverride(
            entity = Item.class,
            association = "bids",
            mode = FetchMode.JOIN
        ))
})

    package org.jpwh.model.fetching.profile;

import org.hibernate.annotations.FetchProfile;
import org.hibernate.annotations.FetchMode;