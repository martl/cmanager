package cmanager.okapi;

import static org.junit.Assert.*;

import org.junit.Test;

import cmanager.geo.Coordinate;
import cmanager.geo.Geocache;

public class OKAPITest
{

    @Test public void testUsernameToUUID() throws Exception
    {
        {
            String uuid = OKAPI.usernameToUUID("This.User.Does.Not.Exist");
            assertEquals(uuid, null);
        }

        {
            String uuid = OKAPI.usernameToUUID("cmanagerTestAccount");
            assertEquals(uuid, "a912cccd-1c60-11e7-8e90-86c6a7325f31");
        }
    }

    @Test public void testGetCache() throws Exception
    {
        {
            Geocache g = OKAPI.getCache("This.Cache.Does.Not.Exist");
            assertTrue(g == null);
        }

        {
            Geocache g = OKAPI.getCache("OC827D");
            assertTrue(g != null);
            assertEquals(g.getName(), "auftanken");
            assertTrue(g.getCoordinate().equals(new Coordinate(49.955717, 8.332967)));
            assertEquals(g.getType().asNiceType(), "Drive-In");
            assertEquals(g.getCodeGC(), null);
            assertTrue(g.getDifficulty() == 1);
            assertTrue(g.getTerrain() == 2);
            assertTrue(g.getArchived() == true);
        }

        {
            Geocache g = OKAPI.getCache("OC11ECF");
            assertTrue(g != null);
            assertEquals(g.getName(), "Gehüpft wie gesprungen");
            assertTrue(g.getCoordinate().equals(new Coordinate(53.019517, 8.5344)));
            assertEquals(g.getType().asNiceType(), "Tradi");
            assertEquals(g.getCodeGC(), "GC46PY8");
            assertTrue(g.getDifficulty() == 2);
            assertTrue(g.getTerrain() == 1.5);
            assertTrue(g.getArchived() == true);
        }
    }

    @Test public void testCompleteCacheDetails() throws Exception
    {
        {
            Geocache g = OKAPI.getCache("OC827D");
            assertTrue(g != null);
            g = OKAPI.completeCacheDetails(g);
            assertEquals(g.getContainer().asGC(), "Nano");
            assertEquals(g.getOwner(), "following");
            assertEquals(g.getListing_short(), "");
            {
                // Adopt once http://redmine.opencaching.de/issues/1045 has beend done.
                final String expected =
                    "<p>ein kleiner Drive-in für zwischendurch<br /><br />Stift mitbringen!</p><p><em>&copy; <a href='https://www.opencaching.de/viewprofile.php?userid=150360'>following</a>, <a href='https://www.opencaching.de/viewcache.php?cacheid=136478'>Opencaching.de</a>, <a href='http://creativecommons.org/licenses/by-nc-nd/3.0/de/'>CC-BY-NC-ND</a>, Stand: ";
                final String listing = g.getListing().substring(0, expected.length());
                assertEquals(listing, expected);
            }
            assertEquals(g.getHint(), "<magnetisch>");
        }

        {
            Geocache g = OKAPI.getCache("OC11ECF");
            assertTrue(g != null);
            g = OKAPI.completeCacheDetails(g);
            assertEquals(g.getContainer().asGC(), "Micro");
            assertEquals(g.getOwner(), "Samsung1");
            assertEquals(g.getListing_short(), "");
            {
                // Adopt once http://redmine.opencaching.de/issues/1045 has beend done.
                // xml version
                final String expected =
                    "<p><span>In Erinnerung an die schöne Zeit, die ich hier als Teenager mit Pferden in diesem schönen Gelände verbringen durfte:<br />Ein kleiner Cache für unterwegs, hoffentlich auch eine kleine Herausforderung für euch ;).<br /><br />Viel Spaß und Erfolg wünschen Samsung1 und Oreas1987.</span></p><p><em>&copy; <a href='https://www.opencaching.de/viewprofile.php?userid=316615'>Samsung1</a>, <a href='https://www.opencaching.de/viewcache.php?cacheid=176512'>Opencaching.de</a>, <a href='http://creativecommons.org/licenses/by-nc-nd/3.0/de/'>CC-BY-NC-ND</a>, Stand: ";
                //            	final String expected = "<p><span>In Erinnerung an die schöne Zeit,
                //            die ich hier als Teenager mit Pferden in diesem schönen Gelände
                //            verbringen durfte:<br />\nEin kleiner Cache für unterwegs, hoffentlich
                //            auch eine kleine Herausforderung für euch ;).<br /><br />\nViel Spaß
                //            und Erfolg wünschen Samsung1 und Oreas1987.</span></p>\n<p><em>&copy;
                //            <a
                //            href='https://www.opencaching.de/viewprofile.php?userid=316615'>Samsung1</a>,
                //            <a
                //            href='https://www.opencaching.de/viewcache.php?cacheid=176512'>Opencaching.de</a>,
                //            <a
                //            href='http://creativecommons.org/licenses/by-nc-nd/3.0/de/'>CC-BY-NC-ND</a>,
                //            Stand: ";
                final String listing = g.getListing().trim().substring(0, expected.length());
                assertEquals(listing, expected);
            }
            assertEquals(g.getHint(), "");
        }
    }
}
