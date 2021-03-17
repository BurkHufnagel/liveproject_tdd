package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;

import java.util.*;

import static com.assetco.search.results.AssetVendorRelationshipLevel.*;
import static com.assetco.search.results.HotspotKey.*;

// This code manages filling the showcase if it's not already set
// it make sure the first partner-lvl vendor with enough assets on the page gets the showcase
class RelationshipBasedOptimizer {
    public void optimize(SearchResults searchResults) {
        Iterator<Asset> iterator = searchResults.getFound().iterator();
        var showcaseFull = searchResults.getHotspot(Showcase).getMembers().size() > 0;
        var showcaseAssets = new ArrayList<Asset>();
        var partnerAssets = new ArrayList<Asset>();
        var goldAssets = new ArrayList<Asset>();
        var silverAssets = new ArrayList<Asset>();

        while (iterator.hasNext()) {
            Asset asset = iterator.next();
            // HACK! trap gold and silver assets for use later
            if (asset.getVendor().getRelationshipLevel() == Gold)
                goldAssets.add(asset);
            else if (asset.getVendor().getRelationshipLevel() == Silver)
                silverAssets.add(asset);

            if (asset.getVendor().getRelationshipLevel() != Partner)
                continue;

            // remember this partner asset
            partnerAssets.add(asset);

            // too many assets in showcase - put in top picks instead...
            if (showcaseAssets.size() >= 5) {
                if (Objects.equals(showcaseAssets.get(0).getVendor(), asset.getVendor()))
                    searchResults.getHotspot(TopPicks).addMember(asset);
            } else {
                // if there are already assets from a different vendor but not enough to hold showcase,
                // clear showcase
                if (showcaseAssets.size() != 0)
                    if (!Objects.equals(showcaseAssets.get(0).getVendor(), asset.getVendor()))
                        if (showcaseAssets.size() < 3)
                            showcaseAssets.clear();

                // add this asset to an empty showcase or showcase with same vendor in it
                // if there's already another vendor, that vendor should take precedence!
                if (showcaseAssets.size() == 0 || Objects.equals(showcaseAssets.get(0).getVendor(), asset.getVendor()))
                    showcaseAssets.add(asset);
            }
        }

        // todo - this does not belong here!!!
        var highValueHotspot = searchResults.getHotspot(HighValue);
        for (var asset : partnerAssets)
            if (!highValueHotspot.getMembers().contains(asset))
                highValueHotspot.addMember(asset);

        // TODO - this needs to be moved to something that only manages the fold
        for (var asset : partnerAssets)
            searchResults.getHotspot(Fold).addMember(asset);

        // only copy showcase assets into hotspot if there are enough for a partner to claim the showcase
        if (!showcaseFull && showcaseAssets.size() >= 3) {
            Hotspot showcaseHotspot = searchResults.getHotspot(Showcase);
            for (Asset asset : showcaseAssets)
                showcaseHotspot.addMember(asset);
        }

        // acw-14339: gold assets should be in high value hotspots if there are no partner assets in search
        for (var asset : goldAssets)
            if (!highValueHotspot.getMembers().contains(asset))
                highValueHotspot.addMember(asset);

        // acw-14341: gold assets should appear in fold box when appropriate
        for (var asset : goldAssets)
            searchResults.getHotspot(Fold).addMember(asset);

        // LOL acw-14511: gold assets should appear in fold box when appropriate
        for (var asset : silverAssets)
            searchResults.getHotspot(Fold).addMember(asset);
    }
}
