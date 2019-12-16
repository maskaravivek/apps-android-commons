package fr.free.nrw.commons

import com.nhaarman.mockito_kotlin.whenever
import fr.free.nrw.commons.di.NetworkingModule.NAMED_COMMONS_CSRF
import fr.free.nrw.commons.upload.FileUtilsWrapper
import fr.free.nrw.commons.wikidata.WikidataClient
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.wikipedia.csrf.CsrfTokenClient
import org.wikipedia.dataclient.Service
import javax.inject.Inject
import javax.inject.Named

class SampleServiceTest {

    @Mock
    internal var wikidataClient: WikidataClient? = null

    @Mock
    @field:[Inject Named("commons-service")]
    internal var service: Service? = null

    @Mock
    @field:[Inject Named(NAMED_COMMONS_CSRF)]
    internal var csrfTokenClient: CsrfTokenClient? = null

    @Mock
    internal var fileUtilWrapper: FileUtilsWrapper? = null

    @InjectMocks
    var sampleService: SampleService? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        `when`(csrfTokenClient!!.tokenBlocking).thenReturn("token")
    }

    @Test(expected = ArithmeticException::class)
    fun sampleMethod1() {
        sampleService!!.sampleMethod1()
    }

    @Test
    fun sampleMethod2() {
        assertEquals("token", sampleService!!.sampleMethod4())
    }

    @Test(expected = ArithmeticException::class)
    fun sampleMethod3() {
        sampleService!!.sampleMethod3()
    }
}