package com.assetco.hotspots.optimization;

import com.assetco.search.results.Asset;
import com.assetco.search.results.AssetVendor;
import com.assetco.search.results.AssetVendorRelationshipLevel;
import com.assetco.search.results.HotspotKey;
import static com.assetco.search.results.HotspotKey.Showcase;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;


class BugsTest {
    @Test
    public void precedingPartnerWithLongTrailingAssetsDoesNotWin() {
        AssetVendor partnerVendor = makeVendor(AssetVendorRelationshipLevel.Partner);
        Asset missing = givenAssetInResultsWithVendor(partnerVendor);

        AssetVendor otherVendor = makeVendor(AssetVendorRelationshipLevel.Partner);
        Asset disruptingAsset = givenAssetInResultsWithVendor(otherVendor);

        List<Asset> expected = getExpectedAssets(partnerVendor);

        whenOptimize();

        thenHotspotDoesNotHave(Showcase, missing);
        thenHotspotHasExactly(Showcase, expected);
    }


    private void thenHotspotHasExactly(HotspotKey key, List<Asset> expected) {
    }


    private void thenHotspotDoesNotHave(HotspotKey key, Asset... missing) {
    }


    private void whenOptimize() {
    }


    private List<Asset> getExpectedAssets(AssetVendor partnerVendor) {
        List<Asset> assets = new ArrayList<>();
        assets.add( givenAssetInResultsWithVendor(partnerVendor) );
        assets.add( givenAssetInResultsWithVendor(partnerVendor) );
        assets.add( givenAssetInResultsWithVendor(partnerVendor) );
        assets.add( givenAssetInResultsWithVendor(partnerVendor) );
        
        return assets;
    }


    private Asset givenAssetInResultsWithVendor(AssetVendor partnerVendor) {
        return null;
    }


    private AssetVendor makeVendor(AssetVendorRelationshipLevel level) {
        return null;
    }

}