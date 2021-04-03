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
        List<Asset> showcaseAssets = new ArrayList<>();
        List<Asset> partnerAssets = new ArrayList<>();
        var goldAssets = new ArrayList<Asset>();
        var silverAssets = new ArrayList<Asset>();

        while (iterator.hasNext()) {
            Asset asset = iterator.next();
            // HACK! trap gold and silver assets for use later
            if (asset.getVendor().getRelationshipLevel() == Gold)
                goldAssets.add(asset);
            else if (asset.getVendor().getRelationshipLevel() == Silver)
                silverAssets.add(asset);

            if (asset.getVendor().getRelationshipLevel() == Partner)
                // remember this partner asset
                partnerAssets.add(asset);
        }

        List<AssetVendor> partners = getPartners(partnerAssets);
        for(AssetVendor partner : partners) {
            List<Asset> potentialShowCaseAssets = getAssetsForPartner(partner, partnerAssets);

            // If there's enough to fill the showcase, we're done looking
            if(potentialShowCaseAssets.size() >= 5) {
                showcaseAssets = potentialShowCaseAssets;
                break;
            }

            // If the current list is greater than the
            if(potentialShowCaseAssets.size() > showcaseAssets.size()) {
                showcaseAssets = potentialShowCaseAssets;
            }
        }

        // if there are too many assets in showcaseAssets - put the extras top picks instead...
        if (showcaseAssets.size() >= 5) {
            for (int index = 5; index < showcaseAssets.size(); index++) {
                searchResults.getHotspot(TopPicks).addMember(showcaseAssets.get(index));
            }

            while (showcaseAssets.size() > 5) {
                showcaseAssets.remove(showcaseAssets.size()-1);
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

    // Get a list of the partners in the order they appear in the assets list.
    private List<AssetVendor> getPartners(List<Asset> partnerAssets) {
        List<AssetVendor> partners = new ArrayList<>();
        List<String> ids = new ArrayList<>();

        for(Asset partnerAsset : partnerAssets) {
            AssetVendor partner = partnerAsset.getVendor();
            if( !ids.contains( partnerAsset.getId()) ) {
                ids.add(partner.getId());
                partners.add(partner);
            }
        }

        return partners;
    }


    // Return a list of the Assets for the specified :Partner
    private List<Asset> getAssetsForPartner(AssetVendor partner, List<Asset> assets) {
        List<Asset> partnerAssets = new ArrayList<>();

        for(Asset asset : assets) {
            if (asset.getVendor().getId().equals(partner.getId()) ) {
                partnerAssets.add(asset);
            }
        }

        return partnerAssets;
    }

}
