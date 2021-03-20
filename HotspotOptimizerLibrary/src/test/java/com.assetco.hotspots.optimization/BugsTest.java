package com.assetco.hotspots.optimization;


import com.assetco.search.results.*;
import org.junit.jupiter.api.*;
import java.math.*;
import java.util.*;

import static com.assetco.search.results.AssetVendorRelationshipLevel.*;
import static com.assetco.search.results.HotspotKey.*;
import static org.junit.jupiter.api.Assertions.*;


public class BugsTest {
    private SearchResults searchResults;


    @BeforeEach
    public void setup() {
        searchResults = new SearchResults();
    }


    @Test
    public void precedingPartnerWithLongTrailingAssetsDoesNotWin() {
        final var partnerVendor = makeVendor(Partner);
        final int maximumShowcaseItems = 5;
        var missing = givenAssetInResultsWithVendor(partnerVendor);
        givenAssetInResultsWithVendor(makeVendor(Partner));
        var expected = givenAssetsInResultsWithVendor(maximumShowcaseItems - 1, partnerVendor);

        whenOptimize();

        thenHotspotDoesNotHave(Showcase, missing);
        thenHotspotHasExactly(Showcase, expected);
    }


    private AssetVendor makeVendor(AssetVendorRelationshipLevel relationshipLevel) {
        return new AssetVendor("", "", relationshipLevel, 1.2f);
    }


    private Asset givenAssetInResultsWithVendor(AssetVendor vendor) {
        Asset asset = getAsset(vendor);
        searchResults.addFound(asset);

        return asset;
    }


    private Asset getAsset(AssetVendor vendor) {
        AssetPurchaseInfo purchaseInfoLast30Days = getPurchaseInfo();
        AssetPurchaseInfo purchaseInfoLast24Hours = getPurchaseInfo();
        List<AssetTopic> topics = new ArrayList<>();

        return new Asset("", "", null, null, purchaseInfoLast30Days, purchaseInfoLast24Hours, topics, vendor);
    }


    private AssetPurchaseInfo getPurchaseInfo() {
        return new AssetPurchaseInfo(5, 3, new Money(BigDecimal.TEN), new Money(BigDecimal.ONE));
    }


    private ArrayList<Asset> givenAssetsInResultsWithVendor(int count, AssetVendor vendor) {
        var result = new ArrayList<Asset>();
        for (var i = 0; i < count; ++i) {
            result.add(givenAssetInResultsWithVendor(vendor));
        }

        return result;
    }


    private void whenOptimize() {
        SearchResultHotspotOptimizer optimizer = new SearchResultHotspotOptimizer();
        optimizer.optimize(searchResults);
    }


    private void thenHotspotDoesNotHave(HotspotKey key, Asset... forbidden) {
        Hotspot hotspot = searchResults.getHotspot(key);

        for(Asset asset : forbidden) {
            assertFalse(hotspot.getMembers().contains(asset));
        }
    }


    private void thenHotspotHasExactly(HotspotKey hotspotKey, List<Asset> expected) {
        Hotspot hotspot = searchResults.getHotspot(hotspotKey);
        List<Asset> hotSpotAssets = hotspot.getMembers();

        assertEquals( hotSpotAssets, expected );
    }
}
