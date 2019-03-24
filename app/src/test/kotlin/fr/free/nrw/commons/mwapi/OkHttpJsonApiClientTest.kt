package fr.free.nrw.commons.mwapi

import com.google.gson.Gson
import fr.free.nrw.commons.BuildConfig
import fr.free.nrw.commons.TestCommonsApplication
import fr.free.nrw.commons.kvstore.JsonKvStore
import junit.framework.Assert.assertEquals
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = [23], application = TestCommonsApplication::class)
class OkHttpJsonApiClientTest {

    private lateinit var testObject: OkHttpJsonApiClient
    private lateinit var toolsForgeServer: MockWebServer
    private lateinit var sparqlServer: MockWebServer
    private lateinit var campaignsServer: MockWebServer
    private lateinit var server: MockWebServer
    private lateinit var sharedPreferences: JsonKvStore
    private lateinit var okHttpClient: OkHttpClient

    @Before
    fun setUp() {
        server = MockWebServer()
        toolsForgeServer = MockWebServer()
        sparqlServer = MockWebServer()
        campaignsServer = MockWebServer()
        okHttpClient = OkHttpClient.Builder().build()
        sharedPreferences = Mockito.mock(JsonKvStore::class.java)
        val toolsForgeUrl = "http://" + toolsForgeServer.hostName + ":" + toolsForgeServer.port + "/"
        val sparqlUrl = "http://" + sparqlServer.hostName + ":" + sparqlServer.port + "/"
        val campaignsUrl = "http://" + campaignsServer.hostName + ":" + campaignsServer.port + "/"
        val serverUrl = "http://" + server.hostName + ":" + server.port + "/"
        testObject = OkHttpJsonApiClient(okHttpClient, HttpUrl.get(toolsForgeUrl), sparqlUrl, campaignsUrl, serverUrl, sharedPreferences, Gson())
    }

    @After
    fun teardown() {
        server.shutdown()
    }

    @Test
    fun getCategoryImages() {
        val mockResponse = MockResponse()
        mockResponse.setResponseCode(200)
        mockResponse.setBody("{\"batchcomplete\":\"\",\"continue\":{\"gcmcontinue\":\"file|30312d30312d32303134202d204d455353455455524d202d205452414445204641495220544f574552202d204652414e4b465552542d204745524d414e59202d2030312e4a50470a30312d30312d32303134202d204d455353455455524d202d205452414445204641495220544f574552202d204652414e4b465552542d204745524d414e59202d2030312e4a5047|32494596\",\"continue\":\"gcmcontinue||\"},\"query\":{\"pages\":{\"49179423\":{\"pageid\":49179423,\"ns\":6,\"title\":\"File:\\\"Broke, baby sick, and car trouble!\\\" - Dorothea Lange's photo of a Missouri family of five in the vicinity of Tracy, California.jpg\",\"imagerepository\":\"local\",\"imageinfo\":[{\"url\":\"https://upload.wikimedia.org/wikipedia/commons/9/91/%22Broke%2C_baby_sick%2C_and_car_trouble%21%22_-_Dorothea_Lange%27s_photo_of_a_Missouri_family_of_five_in_the_vicinity_of_Tracy%2C_California.jpg\",\"descriptionurl\":\"https://commons.wikimedia.org/wiki/File:%22Broke,_baby_sick,_and_car_trouble!%22_-_Dorothea_Lange%27s_photo_of_a_Missouri_family_of_five_in_the_vicinity_of_Tracy,_California.jpg\",\"descriptionshorturl\":\"https://commons.wikimedia.org/w/index.php?curid=49179423\",\"extmetadata\":{\"DateTime\":{\"value\":\"2016-06-01 22:33:46\",\"source\":\"mediawiki-metadata\",\"hidden\":\"\"},\"Categories\":{\"value\":\"1930s automobiles|Adam Cuerden's restorations|Black and white photographs of California|Black and white photographs of automobiles|Environmental migrants|Featured pictures of California|Featured pictures of automobiles|Featured pictures on Wikipedia, English|February 1937 in California|Images from the Library of Congress|Jalopy cars in the Great Depression|Media supported by WikiProject Women in Red - 2016|Migrant workers in the United States during the Great Depression|Nitrate negatives|PD US FSA/OWI|Photographs by Dorothea Lange|Poverty in the United States|Road vehicles photographed in 1937|Telephone poles in the United States\",\"source\":\"commons-categories\",\"hidden\":\"\"},\"ImageDescription\":{\"value\":\"<p>Tracy (vicinity), California. Missouri family of five who are seven months from the drought area on U.S. Highway 99. \\\"Broke, baby sick, and car trouble!\\\" (LOC description) \\n</p>\\n<p>N.B. The sign just visible in front of the truck confirms the <a href=\\\"//commons.wikimedia.org/wiki/File:Depression_migrants_CA_truck.jpg\\\" title=\\\"File:Depression migrants CA truck.jpg\\\">other version on the LoC site</a> is correct, not <a href=\\\"//commons.wikimedia.org/wiki/File:%22Broke,_baby_sick,_and_car_trouble!%22_-_Dorothea_Lange%27s_photo_of_a_Missouri_family_of_five_in_the_vicinity_of_Tracy,_California_-_Original.tif\\\" title=\\\"File:&quot;Broke, baby sick, and car trouble!&quot; - Dorothea Lange's photo of a Missouri family of five in the vicinity of Tracy, California - Original.tif\\\">the mirror image version</a> seen in the original scan this is based on.\\n</p>\",\"source\":\"commons-desc-page\"},\"DateTimeOriginal\":{\"value\":\"1937-02\",\"source\":\"commons-desc-page\"},\"Artist\":{\"value\":\"<bdi><a href=\\\"https://en.wikipedia.org/wiki/en:Dorothea_Lange\\\" class=\\\"extiw\\\" title=\\\"w:en:Dorothea Lange\\\">Dorothea Lange</a>\\n</bdi>\",\"source\":\"commons-desc-page\"},\"LicenseShortName\":{\"value\":\"Public domain\",\"source\":\"commons-desc-page\",\"hidden\":\"\"}}}]},\"49829990\":{\"pageid\":49829990,\"ns\":6,\"title\":\"File:\\\"Chuveir\\u00e3o\\\" na Caverna Timimina.jpg\",\"imagerepository\":\"local\",\"imageinfo\":[{\"url\":\"https://upload.wikimedia.org/wikipedia/commons/7/7c/%22Chuveir%C3%A3o%22_na_Caverna_Timimina.jpg\",\"descriptionurl\":\"https://commons.wikimedia.org/wiki/File:%22Chuveir%C3%A3o%22_na_Caverna_Timimina.jpg\",\"descriptionshorturl\":\"https://commons.wikimedia.org/w/index.php?curid=49829990\",\"extmetadata\":{\"DateTime\":{\"value\":\"2016-06-30 23:13:04\",\"source\":\"mediawiki-metadata\",\"hidden\":\"\"},\"Categories\":{\"value\":\"Bathtubs (speleology)|Featured pictures from Wiki Loves Earth 2016 in Brazil|Featured pictures of S\\u00e3o Paulo|Flowstones|Images from Wiki Loves Earth 2016|Images from Wiki Loves Earth 2016 in Brazil|Media with locations|Pages with maps|Parque Estadual Tur\\u00edstico do Alto Ribeira|Rimstones|Self-published work|Uploaded via Campaign:wle-br\",\"source\":\"commons-categories\",\"hidden\":\"\"},\"GPSLatitude\":{\"value\":\"-24.460000\",\"source\":\"commons-desc-page\",\"hidden\":\"\"},\"GPSLongitude\":{\"value\":\"-48.600000\",\"source\":\"commons-desc-page\",\"hidden\":\"\"},\"ImageDescription\":{\"value\":\"Forma\\u00e7\\u00e3o conhecida como \\\"Chuveir\\u00e3o\\\" na Caverna Timimina, PETAR - N\\u00facleo Caboclos\",\"source\":\"commons-desc-page\"},\"DateTimeOriginal\":{\"value\":\"2015-07-14 19:04:06\",\"source\":\"commons-desc-page\"},\"Artist\":{\"value\":\"<a href=\\\"//commons.wikimedia.org/wiki/User:Rafael_Rodrigues_Camargo\\\" title=\\\"User:Rafael Rodrigues Camargo\\\">Rafael Rodrigues Camargo</a>\",\"source\":\"commons-desc-page\"},\"LicenseShortName\":{\"value\":\"CC BY-SA 4.0\",\"source\":\"commons-desc-page\",\"hidden\":\"\"}}}]},\"4406048\":{\"pageid\":4406048,\"ns\":6,\"title\":\"File:\\\"The School of Athens\\\" by Raffaello Sanzio da Urbino.jpg\",\"imagerepository\":\"local\",\"imageinfo\":[{\"url\":\"https://upload.wikimedia.org/wikipedia/commons/4/49/%22The_School_of_Athens%22_by_Raffaello_Sanzio_da_Urbino.jpg\",\"descriptionurl\":\"https://commons.wikimedia.org/wiki/File:%22The_School_of_Athens%22_by_Raffaello_Sanzio_da_Urbino.jpg\",\"descriptionshorturl\":\"https://commons.wikimedia.org/w/index.php?curid=4406048\",\"extmetadata\":{\"DateTime\":{\"value\":\"2013-04-13 15:12:11\",\"source\":\"mediawiki-metadata\",\"hidden\":\"\"},\"Categories\":{\"value\":\"Artworks with Wikidata item|CC-PD-Mark|Featured pictures of art|Featured pictures of painting|Featured pictures of the Vatican City|Featured pictures on Wikipedia, English|Featured pictures on Wikipedia, Persian|Featured pictures on Wikipedia, Turkish|Featured pictures on Wikipedia, Vietnamese|Images with 10+ annotations|Images with annotations|PD-Art (PD-old-auto-expired)|PD-old-100-expired|The School of Athens\",\"source\":\"commons-categories\",\"hidden\":\"\"},\"Artist\":{\"value\":\"<bdi><a href=\\\"https://en.wikipedia.org/wiki/en:Raphael\\\" class=\\\"extiw\\\" title=\\\"w:en:Raphael\\\">Raphael</a>\\n</bdi>\",\"source\":\"commons-desc-page\"},\"ImageDescription\":{\"value\":\"<div class=\\\"description\\\">\\n<span style=\\\"font-size:0.9em\\\">Italian:  <i>Scuola di Atene</i></span><span style=\\\"font-weight:bold\\\"><br><i><a href=\\\"https://en.wikipedia.org/wiki/School_of_Athens\\\" class=\\\"extiw\\\" title=\\\"w:School of Athens\\\">The School of Athens</a></i></span><div style=\\\"display: none;\\\">label QS:Lit,\\\"Scuola di Atene\\\"</div>\\n<div style=\\\"display: none;\\\">label QS:Len,\\\"<a href=\\\"https://en.wikipedia.org/wiki/School_of_Athens\\\" class=\\\"extiw\\\" title=\\\"w:School of Athens\\\">The School of Athens</a>\\\"</div>\\n<div style=\\\"display: none;\\\">label QS:Les,\\\"<a href=\\\"https://es.wikipedia.org/wiki/La_escuela_de_Atenas\\\" class=\\\"extiw\\\" title=\\\"es:La escuela de Atenas\\\">La escuela de Atenas</a>.\\\"</div>\\n</div>\",\"source\":\"commons-desc-page\"},\"DateTimeOriginal\":{\"value\":\"1511<div style=\\\"display: none;\\\">date QS:P571,+1511-00-00T00:00:00Z/9</div>\",\"source\":\"commons-desc-page\"},\"LicenseShortName\":{\"value\":\"Public domain\",\"source\":\"commons-desc-page\",\"hidden\":\"\"}}}]},\"38243931\":{\"pageid\":38243931,\"ns\":6,\"title\":\"File:'One of the wards in the hospital at Scutari'. Wellcome M0007724 - restoration, cropped.jpg\",\"imagerepository\":\"local\",\"imageinfo\":[{\"url\":\"https://upload.wikimedia.org/wikipedia/commons/9/97/%27One_of_the_wards_in_the_hospital_at_Scutari%27._Wellcome_M0007724_-_restoration%2C_cropped.jpg\",\"descriptionurl\":\"https://commons.wikimedia.org/wiki/File:%27One_of_the_wards_in_the_hospital_at_Scutari%27._Wellcome_M0007724_-_restoration,_cropped.jpg\",\"descriptionshorturl\":\"https://commons.wikimedia.org/w/index.php?curid=38243931\",\"extmetadata\":{\"DateTime\":{\"value\":\"2015-02-06 15:37:28\",\"source\":\"mediawiki-metadata\",\"hidden\":\"\"},\"Categories\":{\"value\":\"1850s interiors in art|Adam Cuerden's restorations|Artworks without Wikidata item|Featured pictures of Turkey|Featured pictures on Wikipedia, Bengali|Featured pictures on Wikipedia, English|Files from Wellcome Images|Florence Nightingale in art|Hospital wards|Images uploaded by F\\u00e6|Lithographs in the Wellcome Collection\",\"source\":\"commons-categories\",\"hidden\":\"\"},\"Artist\":{\"value\":\"<div class=\\\"fn value\\\">\\n<ul>\\n<li>\\n<a href=\\\"https://en.wikipedia.org/wiki/William_Simpson_(artist)\\\" class=\\\"extiw\\\" title=\\\"en:William Simpson (artist)\\\">William Simpson</a> (artist, 1823\\u20131899)</li>\\n<li>E. Walker (lithographer, lifespan unknown, working for Day &amp; Son)</li>\\n<li>Publishers:  Paul and Dominic Colnaghi, London; Goupil &amp; Cie, Paris; Otto Wiegel, Leipzig.</li>\\n<li>Restoration by <a href=\\\"//commons.wikimedia.org/wiki/User:Adam_Cuerden\\\" title=\\\"User:Adam Cuerden\\\">Adam Cuerden</a>\\n</li>\\n</ul>\\n</div>\",\"source\":\"commons-desc-page\"},\"ImageDescription\":{\"value\":\"Iconographic Collections. Keywords: E. Walker; Florence Nightingale; W.J. Simpson\",\"source\":\"commons-desc-page\"},\"DateTimeOriginal\":{\"value\":\"\\\"Published April 21<sup>st</sup> 1856\\\"\",\"source\":\"commons-desc-page\"},\"LicenseShortName\":{\"value\":\"CC BY 4.0\",\"source\":\"commons-desc-page\",\"hidden\":\"\"}}}]},\"45353950\":{\"pageid\":45353950,\"ns\":6,\"title\":\"File:0 A.D. Alpha 12 Loucetios.webm\",\"imagerepository\":\"local\",\"imageinfo\":[{\"url\":\"https://upload.wikimedia.org/wikipedia/commons/4/49/0_A.D._Alpha_12_Loucetios.webm\",\"descriptionurl\":\"https://commons.wikimedia.org/wiki/File:0_A.D._Alpha_12_Loucetios.webm\",\"descriptionshorturl\":\"https://commons.wikimedia.org/w/index.php?curid=45353950\",\"extmetadata\":{\"DateTime\":{\"value\":\"2015-12-01 02:57:39\",\"source\":\"mediawiki-metadata\",\"hidden\":\"\"},\"Categories\":{\"value\":\"Featured videos|Files from external sources with reviewed licenses|Media of the day|Video display resolution 1280 x 720|Video files of 0 A.D.|WebM videos\",\"source\":\"commons-categories\",\"hidden\":\"\"},\"ImageDescription\":{\"value\":\"Release video about the \\\"<i>0 A.D. Alpha 12 Loucetios</i>\\\", the twelfth alpha version of <b><a href=\\\"https://en.wikipedia.org/wiki/0_A.D._(video_game)\\\" class=\\\"extiw\\\" title=\\\"w:0 A.D. (video game)\\\">0 A.D.</a></b>, a free, open-source game of ancient warfare. The accompanying music is \\\"<a href=\\\"//commons.wikimedia.org/wiki/File:Omri_Lahav_0_A.D._OST_-_Harvest_Festival.flac\\\" title=\\\"File:Omri Lahav 0 A.D. OST - Harvest Festival.flac\\\">Harvest Festival</a>\\\", a song by <a rel=\\\"nofollow\\\" class=\\\"external text\\\" href=\\\"http://www.omrilahav.com/\\\">Omri Lahav</a>, also published under a CC-BY-SA 3.0 license.\",\"source\":\"commons-desc-page\"},\"DateTimeOriginal\":{\"value\":\"2012-12-16\",\"source\":\"commons-desc-page\"},\"Artist\":{\"value\":\"<a rel=\\\"nofollow\\\" class=\\\"external text\\\" href=\\\"http://wildfiregames.com/\\\">Wildfire Games</a>, an international group of volunteer game developers\",\"source\":\"commons-desc-page\"},\"LicenseShortName\":{\"value\":\"CC BY-SA 3.0\",\"source\":\"commons-desc-page\",\"hidden\":\"\"}}}]},\"51325743\":{\"pageid\":51325743,\"ns\":6,\"title\":\"File:0000140 Wat Arun 01.jpg\",\"imagerepository\":\"local\",\"imageinfo\":[{\"url\":\"https://upload.wikimedia.org/wikipedia/commons/3/3b/0000140_Wat_Arun_01.jpg\",\"descriptionurl\":\"https://commons.wikimedia.org/wiki/File:0000140_Wat_Arun_01.jpg\",\"descriptionshorturl\":\"https://commons.wikimedia.org/w/index.php?curid=51325743\",\"extmetadata\":{\"DateTime\":{\"value\":\"2016-09-11 16:48:14\",\"source\":\"mediawiki-metadata\",\"hidden\":\"\"},\"Categories\":{\"value\":\"2016 in Bangkok|Blue hour in Thailand|Clouds at sunset|Cultural heritage monuments in Thailand with known IDs|Exposure time 30 sec|Featured pictures from Wiki Loves Monuments 2016|Featured pictures of Bangkok|Featured pictures on Wikipedia, English|Featured pictures on Wikipedia, Turkish|Images from Wiki Loves Monuments 2016|Images from Wiki Loves Monuments 2016 in Thailand|Images with extracted images|Long exposure photography in architecture|Media with locations|Night in Bangkok|Nominated pictures for Wiki Loves Monuments International|Pages with maps|Reflections at night|Reflections in puddles|Self-published work|September 2016 Thailand photographs|Taken with Nikon D810|Uploaded via Campaign:wlm-th|Wat Arun, entrance to ordination hall|Water reflections of buildings in Thailand\",\"source\":\"commons-categories\",\"hidden\":\"\"},\"GPSLatitude\":{\"value\":\"13.744033\",\"source\":\"commons-desc-page\",\"hidden\":\"\"},\"GPSLongitude\":{\"value\":\"100.488413\",\"source\":\"commons-desc-page\",\"hidden\":\"\"},\"ImageDescription\":{\"value\":\"This is a photo of a monument in Thailand identified by the ID\",\"source\":\"commons-desc-page\"},\"DateTimeOriginal\":{\"value\":\"2016-09-11 18:44:50\",\"source\":\"commons-desc-page\"},\"Artist\":{\"value\":\"<a href=\\\"//commons.wikimedia.org/w/index.php?title=User:Jane3030&amp;action=edit&amp;redlink=1\\\" class=\\\"new\\\" title=\\\"User:Jane3030 (page does not exist)\\\">BerryJ</a>\",\"source\":\"commons-desc-page\"},\"LicenseShortName\":{\"value\":\"CC BY-SA 4.0\",\"source\":\"commons-desc-page\",\"hidden\":\"\"}}}]},\"24259710\":{\"pageid\":24259710,\"ns\":6,\"title\":\"File:001117 15-44-2002-To-grupper-rosa-Qajar-Fliser2.jpg\",\"imagerepository\":\"local\",\"imageinfo\":[{\"url\":\"https://upload.wikimedia.org/wikipedia/commons/9/91/001117_15-44-2002-To-grupper-rosa-Qajar-Fliser2.jpg\",\"descriptionurl\":\"https://commons.wikimedia.org/wiki/File:001117_15-44-2002-To-grupper-rosa-Qajar-Fliser2.jpg\",\"descriptionshorturl\":\"https://commons.wikimedia.org/w/index.php?curid=24259710\",\"extmetadata\":{\"DateTime\":{\"value\":\"2013-01-27 12:53:42\",\"source\":\"mediawiki-metadata\",\"hidden\":\"\"},\"Categories\":{\"value\":\"Artworks without Wikidata item|Azulejo patterns|Azulejos of flowers|CC-PD-Mark|CC-Zero|Featured pictures of Iran|Featured pictures of mosaics|Featured pictures on Wikipedia, Persian|PD Old|Paintings of birds|Qajar art|Rectangular objects|Self-published work|Symmetric objects|The David Collection|Tiles in Iran|Vases in art\",\"source\":\"commons-categories\",\"hidden\":\"\"},\"Artist\":{\"value\":\"Owji\",\"source\":\"commons-desc-page\"},\"ImageDescription\":{\"value\":\"Two panels of earthenware tiles painted with polychrome glazes over a white glaze. Iran; 19th century first half. Each panel: H: 81.5; W: 30.5 cm\",\"source\":\"commons-desc-page\"},\"DateTimeOriginal\":{\"value\":\"First half of 19th\",\"source\":\"commons-desc-page\"},\"LicenseShortName\":{\"value\":\"Public domain\",\"source\":\"commons-desc-page\",\"hidden\":\"\"}}}]},\"75048602\":{\"pageid\":75048602,\"ns\":6,\"title\":\"File:004 2018 05 14 Extremes Wetter.jpg\",\"imagerepository\":\"local\",\"imageinfo\":[{\"url\":\"https://upload.wikimedia.org/wikipedia/commons/0/09/004_2018_05_14_Extremes_Wetter.jpg\",\"descriptionurl\":\"https://commons.wikimedia.org/wiki/File:004_2018_05_14_Extremes_Wetter.jpg\",\"descriptionshorturl\":\"https://commons.wikimedia.org/w/index.php?curid=75048602\",\"extmetadata\":{\"DateTime\":{\"value\":\"2019-02-10 08:50:40\",\"source\":\"mediawiki-metadata\",\"hidden\":\"\"},\"Categories\":{\"value\":\"2018 in Rhineland-Palatinate|Cloud-to-cloud lightning|Exposure time 8 sec|Featured pictures by others nominated by Yann Forget|Featured pictures of Rhineland-Palatinate|Germany photographs taken on 2018-05-14|Images by F. Riedelio|Lightning in Germany|Lightning strikes|Long exposure photography|Media with locations|Pages with maps|Photo challenge winners|Photographic silhouettes of trees|Photographs taken on 2018-05-14|Purple sky|Self-published work|Taken with Canon EOS 6D Mark II|Taken with Tamron SP AF 24-70mm F/2.8 Di VC USD|Uploaded with LrMediaWiki\",\"source\":\"commons-categories\",\"hidden\":\"\"},\"GPSLatitude\":{\"value\":\"49.105219\",\"source\":\"commons-desc-page\",\"hidden\":\"\"},\"GPSLongitude\":{\"value\":\"7.995839\",\"source\":\"commons-desc-page\",\"hidden\":\"\"},\"ImageDescription\":{\"value\":\"<p><a href=\\\"https://en.wikipedia.org/wiki/Lightning\\\" class=\\\"extiw\\\" title=\\\"en:Lightning\\\">Lightning strike</a> in <a href=\\\"https://en.wikipedia.org/wiki/Pinales\\\" class=\\\"extiw\\\" title=\\\"en:Pinales\\\">conifer</a>.\\n</p>\",\"source\":\"commons-desc-page\"},\"DateTimeOriginal\":{\"value\":\"Taken on\\u00a014.05.2018 21:24:28\",\"source\":\"commons-desc-page\"},\"Artist\":{\"value\":\"<a href=\\\"//commons.wikimedia.org/wiki/User:F._Riedelio\\\" title=\\\"User:F. Riedelio\\\">Friedrich Haag</a>\",\"source\":\"commons-desc-page\"},\"LicenseShortName\":{\"value\":\"CC BY-SA 4.0\",\"source\":\"commons-desc-page\",\"hidden\":\"\"}}}]},\"28822769\":{\"pageid\":28822769,\"ns\":6,\"title\":\"File:01 Calanche Piana.jpg\",\"imagerepository\":\"local\",\"imageinfo\":[{\"url\":\"https://upload.wikimedia.org/wikipedia/commons/0/01/01_Calanche_Piana.jpg\",\"descriptionurl\":\"https://commons.wikimedia.org/wiki/File:01_Calanche_Piana.jpg\",\"descriptionshorturl\":\"https://commons.wikimedia.org/w/index.php?curid=28822769\",\"extmetadata\":{\"DateTime\":{\"value\":\"2013-10-03 10:45:15\",\"source\":\"mediawiki-metadata\",\"hidden\":\"\"},\"Categories\":{\"value\":\"Calanques of Piana|Featured pictures by Myrabella|Featured pictures of Corse-du-Sud|Featured pictures of landscapes|Landscapes of Corse-du-Sud|Media with locations|Pages with maps|Self-published work\",\"source\":\"commons-categories\",\"hidden\":\"\"},\"GPSLatitude\":{\"value\":\"42.246930\",\"source\":\"commons-desc-page\",\"hidden\":\"\"},\"GPSLongitude\":{\"value\":\"8.654680\",\"source\":\"commons-desc-page\",\"hidden\":\"\"},\"ImageDescription\":{\"value\":\"<a href=\\\"https://en.wikipedia.org/wiki/Piana\\\" class=\\\"extiw\\\" title=\\\"w:Piana\\\">Piana</a>, Southern Corsica, France\\u00a0: the <a href=\\\"https://en.wikipedia.org/wiki/Calanques_de_Piana\\\" class=\\\"extiw\\\" title=\\\"w:Calanques de Piana\\\">Calanques of Piana</a>.\",\"source\":\"commons-desc-page\"},\"DateTimeOriginal\":{\"value\":\"2013-08-19\",\"source\":\"commons-desc-page\"},\"Artist\":{\"value\":\"<a href=\\\"//commons.wikimedia.org/wiki/User:Myrabella\\\" title=\\\"User:Myrabella\\\">Myrabella</a>\",\"source\":\"commons-desc-page\"},\"LicenseShortName\":{\"value\":\"CC BY-SA 3.0\",\"source\":\"commons-desc-page\",\"hidden\":\"\"}}}]},\"27597836\":{\"pageid\":27597836,\"ns\":6,\"title\":\"File:01 Gorges du Tarn Roc des Hourtous.jpg\",\"imagerepository\":\"local\",\"imageinfo\":[{\"url\":\"https://upload.wikimedia.org/wikipedia/commons/5/54/01_Gorges_du_Tarn_Roc_des_Hourtous.jpg\",\"descriptionurl\":\"https://commons.wikimedia.org/wiki/File:01_Gorges_du_Tarn_Roc_des_Hourtous.jpg\",\"descriptionshorturl\":\"https://commons.wikimedia.org/w/index.php?curid=27597836\",\"extmetadata\":{\"DateTime\":{\"value\":\"2013-08-06 20:55:30\",\"source\":\"mediawiki-metadata\",\"hidden\":\"\"},\"Categories\":{\"value\":\"2013 in Loz\\u00e8re|Featured pictures by Myrabella|Featured pictures of Loz\\u00e8re|Featured pictures of landscapes|Geomorphology of Loz\\u00e8re|Landforms of Loz\\u00e8re|Landscapes of Loz\\u00e8re|Les D\\u00e9troits (Gorges du Tarn)|Meanders in France|Media with locations|Pages with maps|Rock formations in Loz\\u00e8re|Self-published work|Tarn River in La Mal\\u00e8ne|Views from Roc des Hourtous\",\"source\":\"commons-categories\",\"hidden\":\"\"},\"GPSLatitude\":{\"value\":\"44.291970\",\"source\":\"commons-desc-page\",\"hidden\":\"\"},\"GPSLongitude\":{\"value\":\"3.289930\",\"source\":\"commons-desc-page\",\"hidden\":\"\"},\"ImageDescription\":{\"value\":\"The <a href=\\\"https://en.wikipedia.org/wiki/Gorges_du_Tarn\\\" class=\\\"extiw\\\" title=\\\"en:Gorges du Tarn\\\">Gorges du Tarn</a> in their narrowest part, called <i>les D\\u00e9troits</i> (the Straits), as seen from the <i>Roc des Hourtous</i> (Loz\\u00e8re, France)\",\"source\":\"commons-desc-page\"},\"DateTimeOriginal\":{\"value\":\"2013-07-27\",\"source\":\"commons-desc-page\"},\"Artist\":{\"value\":\"<a href=\\\"//commons.wikimedia.org/wiki/User:Myrabella\\\" title=\\\"User:Myrabella\\\">Myrabella</a>\",\"source\":\"commons-desc-page\"},\"LicenseShortName\":{\"value\":\"CC BY-SA 3.0\",\"source\":\"commons-desc-page\",\"hidden\":\"\"}}}]}}}}")
        server.enqueue(mockResponse)
        val categoryImages = testObject.getCategoryImages("Watercraft moored off shore")
        assertEquals(categoryImages!!.blockingGet().size, 10)
    }
}