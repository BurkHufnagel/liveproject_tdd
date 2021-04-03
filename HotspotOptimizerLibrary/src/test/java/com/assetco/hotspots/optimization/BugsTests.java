package com.assetco.hotspots.optimization;

import com.assetco.hotspots.optimization.*;
import com.assetco.search.results.*;
import org.junit.jupiter.api.*;

import java.math.*;
import java.util.*;

import static com.assetco.search.results.AssetVendorRelationshipLevel.*;
import static com.assetco.search.results.HotspotKey.*;
import static org.junit.jupiter.api.Assertions.*;

// Note that the test doesn't actually "do" anything directly instead it tells another method
// to do something. Even though this is not absolutely necessary, it allows me to clearly express
// the critical aspects of the scenario being tested without being tied to any particular
// implementation details.
public class BugsTests {
    private final int maximumShowcaseItems = 5;
    private SearchResults searchResults;
    private SearchResultHotspotOptimizer optimizer;
    private AssetVendor bigShotz;
    private AssetVendor celebPix;

    // Mark this method to be run once before each test
    @BeforeEach
    public void setUp() {
        // While it is not strictly necessary, yet, to initialize these items separately,
        // I find it helps keep distracting "assumption"-type implementation details out
        // of the body of my tests - even if I have only one test.
        optimizer = new SearchResultHotspotOptimizer();
        searchResults = new SearchResults();
        bigShotz = makeVendor("1", "BigShotz!", Partner);
        celebPix = makeVendor("2", "CelebPix", Partner);
    }

    // Mark the method as a test so it will be executed by the test runner
    @Test
    public void precedingPartnerWithLongTrailingAssetsDoesNotWin() {
        // I don't need to track this, yet, but I like my tests to be very explanatory
        var missingBigShotzAsset = givenAssetInResultsWithVendor(bigShotz);

        // This is the "salt" that makes the system work differently from how business expected
        givenAssetInResultsWithVendor(celebPix);

        // This is what is actually put in the showcase box
        var expected = givenAssetsInResultsWithVendor(4, bigShotz);
        expected.add(0, missingBigShotzAsset);

        whenOptimize();

        thenHotspotShouldHave(Showcase, missingBigShotzAsset);
        thenHotspotHasExactly(Showcase, expected);
    }

    // **************************************************
    // * All these methods simplify the above test and  *
    // * will make other, related tests easier to write *
    // **************************************************

    private AssetVendor makeVendor(String partnerId, String partnerName, AssetVendorRelationshipLevel relationshipLevel) {
        return new AssetVendor(partnerId, partnerName, relationshipLevel, 1);
    }

    private Asset givenAssetInResultsWithVendor(AssetVendor vendor) {
        Asset result = getAsset(vendor);
        searchResults.addFound(result);
        return result;
    }

    private Asset getAsset(AssetVendor vendor) {
        return new Asset("anything", "anything", null, null, getPurchaseInfo(), getPurchaseInfo(), new ArrayList<>(), vendor);
    }

    private AssetPurchaseInfo getPurchaseInfo() {
        return new AssetPurchaseInfo(0, 0,
                new Money(new BigDecimal("0")),
                new Money(new BigDecimal("0")));
    }

    private void thenHotspotHasExactly(HotspotKey hotspotKey, List<Asset> expected) {
        Assertions.assertArrayEquals(expected.toArray(), searchResults.getHotspot(hotspotKey).getMembers().toArray());
    }

    private ArrayList<Asset> givenAssetsInResultsWithVendor(int count, AssetVendor vendor) {
        var result = new ArrayList<Asset>();
        for (var i = 0; i < count; ++i) {
            result.add(givenAssetInResultsWithVendor(vendor));
        }
        return result;
    }

    private void whenOptimize() {
        optimizer.optimize(searchResults);
    }

    private void thenHotspotShouldHave(HotspotKey key, Asset... expected) {
        for (var asset : expected)
            assertTrue(searchResults.getHotspot(key).getMembers().contains(asset));
    }
}
