package cacheheaders

import java.text.SimpleDateFormat

import com.grailsrocks.cacheheaders.TestController
import grails.test.mixin.integration.IntegrationTestMixin
import grails.test.mixin.*
import org.junit.*
import test.*
import static org.junit.Assert.*
import grails.util.*
import org.springframework.beans.factory.annotation.*
import grails.plugins.cacheheaders.*

@TestMixin(IntegrationTestMixin)
class CacheMethodsTests {

	private static final String RFC1123_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz" // Always GMT


	private TestController con = new TestController()

	@Autowired
	CacheHeadersService cacheHeadersService

	void setUp() {
		GrailsWebMockUtil.bindMockWebRequest()
		con.cacheHeadersService = cacheHeadersService 
	}

	void testPresetCanTurnCachingOff() {
		grails.util.Holders.config.cache.headers.presets.presetDeny = false

		con.presetTest1()

		assertEquals 'no-cache, no-store', con.response.getHeader('Cache-Control')
		assertNotNull con.response.getHeader('Expires')
		assertEquals 'no-cache', con.response.getHeader('Pragma')
	}

	void testValidUntil() {
		con.validUntilTest1()

		def d = con.request.getAttribute('test_validUntil')
		assertEquals d.time.toString(), con.response.getHeader('Expires')
	}

	void testValidFor() {
		con.validForTest1()

		def cc = con.response.getHeader('Cache-Control').tokenize(',')*.trim()
		def ma = cc.find { it.startsWith('max-age=') }
		assertNotNull "Did not have max-age", ma
		assertEquals con.request.getAttribute('test_validFor'), (ma-'max-age=').toInteger()
	}

	void testValidForNegative() {
		con.validForTestNeg()

		def cc = con.response.getHeader('Cache-Control').tokenize(',')*.trim()
		def ma = cc.find { it.startsWith('max-age=') }
		assertNotNull "Did not have max-age", ma
		assertEquals 0, (ma-'max-age=').toInteger()
	}

	void testValidUntilNegative() {
		con.validUntilTestNeg()

		def cc = con.response.getHeader('Cache-Control').tokenize(',')*.trim()
		def ma = cc.find { it.startsWith('max-age=') }
		assertNotNull "Did not have max-age", ma
		assertEquals 0, (ma-'max-age=').toInteger()
	}

	void testCombinedStoreAndShareDefault() {
		// If we set store false, it should also default to share: private, specifying both
		con.combinedStoreAndShareDefaultTest()

		def ccParts = con.response.getHeader('Cache-Control').tokenize(',')*.trim()
		assertTrue "Did not contain 'private'", ccParts.contains('private')
		assertTrue "Did not contain 'no-store'", ccParts.contains('no-store')
	}

	private String dateToHTTPDate(date) {
		def v = new SimpleDateFormat(RFC1123_DATE_FORMAT, Locale.ENGLISH)
		v.timeZone = TimeZone.getTimeZone('GMT')
		return v.format(date)
	}
}
