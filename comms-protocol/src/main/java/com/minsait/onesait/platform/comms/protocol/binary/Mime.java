/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.comms.protocol.binary;

public enum Mime {

	APPLICATION_ATOM("application/atom+xml"), APPLICATION_VND("application/vnd.dart"), APPLICATION_ECMASCRIPT(
			"application/ecmascript"), APPLICATION_EDIX12("application/EDI-X12"), APPLICATION_EDIFACT(
					"application/EDIFACT"), APPLICATION_JSON("application/json"), APPLICATION_JACASCRIPT(
							"application/javascript"), APPLICATION_OCTET("application/octet-stream"), APPLICATION_OGG(
									"application/ogg"), APPLICATION_DASH("application/dash+xml"), APPLICATION_PDF(
											"application/pdf"), APPLICATION_POSTCRIPT(
													"application/postscript"), APPLICATION_RDF(
															"application/rdf+xml"), APPLICATION_RSS(
																	"application/rss+xml"), APPLICATION_SOAP(
																			"application/soap+xml"), APPLICATION_FONT(
																					"application/font-woff"), APPLICATION_XHTML(
																							"application/xhtml+xml"), APPLICATION_XML(
																									"application/xml"), APPLICATION_XML_DTD(
																											"application/xml-dtd"), APPLICATION_XOP(
																													"application/xop+xml"), APPLICATION_ZIP(
																															"application/zip"), APPLICATION_GZIP(
																																	"application/gzip"), APPLICATION_EXAMPLE(
																																			"application/example"), APPLICATION_X_NACL(
																																					"application/x-nacl"), APPLICATION_X_PNACL(
																																							"application/x-pnacl"), APPLICATION_SMIL(
																																									"application/smil+xml"),

	APPLICATION_VND_DEBIAN("application/vnd.debian.binary-package"), APPLICATION_VND_OASIS_TEXT(
			"application/vnd.oasis.opendocument.text"), APPLICATION_VND_OASIS_SPREADSHEET(
					"application/vnd.oasis.opendocument.spreadsheet"), APPLICATION_VND_OASIS_PRESENTATION(
							"application/vnd.oasis.opendocument.presentation"), APPLICATION_VND_OASIS_GRAPHICS(
									"application/vnd.oasis.opendocument.graphics"), APPLICATION_VND_MS_EXCEL(
											"application/vnd.ms-excel"), APPLICATION_VND_OPENXMLFORMART_SHEET(
													"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), APPLICATION_VND_MS_POWERPOINT(
															"application/vnd.ms-powerpoint"), APPLICATION_VND_OPENXMLFORMART_PRESENTATION(
																	"application/vnd.openxmlformats-officedocument.presentationml.presentation"), APPLICATION_VND_MOZILLA(
																			"application/vnd.mozilla.xul+xml"), APPLICATION_VND_GOOGLE_EARTH_XML(
																					"application/vnd.google-earth.kml+xml"), APPLICATION_VND_GOOGLE_EARTH(
																							"application/vnd.google-earth.kmz"), APPLICATION_VND_ANDROID(
																									"application/vnd.android.package-archive"), APPLICATION_VND_MS_XPS(
																											"application/vnd.ms-xpsdocument"),

	APPLICATION_X_7Z("application/x-7z-compressed"), APPLICATION_X_CHROME(
			"application/x-chrome-extension"), APPLICATION_X_DVI("application/x-dvi"), APPLICATION_X_FONT(
					"application/x-font-ttf"), APPLICATION_X_JAVASCRIPT(
							"application/x-javascript"), APPLICATION_X_LATEX(
									"application/x-latex"), APPLICATION_X_MPEGURL(
											"application/x-mpegURL"), APPLICATION_X_RAR(
													"application/x-rar-compressed"), APPLICATION_X_SHOCKWAVE(
															"application/x-shockwave-flash"), APPLICATION_X_STUFFIT(
																	"application/x-stuffit"), APPLICATION_X_TAR(
																			"application/x-tar"), APPLICATION_X_WWW(
																					"application/x-www-form-urlencoded"), APPLICATION_X_XINSTALL(
																							"application/x-xpinstall"), APPLICATION_X_ACC(
																									"audio/x-aac"), APPLICATION_X_CAF(
																											"audio/x-caf"), APPLICATION_X_XCF(
																													"image/x-xcf"), APPLICATION_X_RPC(
																															"text/x-gwt-rpc"), APPLICATION_X_JQUERY(
																																	"text/x-jquery-tmpl"), APPLICATION_X_MARKDOWN(
																																			"text/x-markdown"), APPLICATION_X_PKCS12(
																																					"application/x-pkcs12"),

	AUDIO_BASIV("audio/basic"), AUDIO_L24("audio/L24"), AUDIO_MP4("audio/mp4"), AUDIO_MPEG("audio/mpeg"), AUDIO_OGG(
			"audio/ogg"), AUDIO_FLAC("audio/flac"), AUDIO_OPUS("audio/opus"), AUDIO_VORBIS(
					"audio/vorbis"), AUDIO_VND_RN("audio/vnd.rn-realaudio"), AUDIO_VND_WAVE(
							"audio/vnd.wave"), AUDIO_WEBM("audio/webm"), AUDIO_EXAMPLE("audio/example"),

	EXAMPLE("EXAMPLE"),

	IMAGE_GIF("/gif"), IMAGE_JPEG("image/jpeg"), IMAGE_PJPEG("image/pjpeg"), IMAGE_PNG("image/png"), IMAGE_SVG(
			"image/svg+xml"), IMAGE_TIFF("image/tiff"), IMAGE_VND("image/vnd.djvu"), IMAGE_EXAMPLE("image/example"),

	MESSAGE_HTTP("message/http"), MESSAGE_IMDN("message/imdn+xml"), MESSAGE_PARTIAL("message/partial"), MESSAGE_RFC822(
			"message/rfc822"), MESSAGE_EXAMPLE("message/example"),

	MODEL_IGES("model/iges"), MODEL_MESH("model/mesh"), MODEL_VRML("model/vrml"), MODEL_X3D_BINARY(
			"model/x3d+binary"), MODEL_X3D_FASTINFOSET("model/x3d+fastinfoset"), MODEL_X3D_VRML(
					"model/x3d-vrml"), MODEL_X3D_XML("model/x3d+xml"), MODEL_EXAMPLE("model/example"),

	MULTIPART_MIXED("multipart/mixed"), MULTIPART_ALTERNATIVE("multipart/alternative"), MULTIPART_RELATED(
			"multipart/related"), MULTIPART_FORM("multipart/form-data"), MULTIPART_SIGNED(
					"multipart/signed"), MULTIPART_ENCRYPTED(
							"multipart/encrypted"), MULTIPART_EXAMPLE("multipart/example"),

	TEXT_CMD("text/cmd"), TEXT_CSS("text/css"), TEXT_CSV("text/csv"), TEXT_EXAMPLE("text/example"), TEXT_HTML(
			"text/html"), TEXT_PLAIN("text/plain"), TEXT_RTF("text/rtf"), TEXT_VCARD(
					"text/vcard"), TEXT_VND_A("text/vnd.a"), TEXT_VND_ABC("text/vnd.abc"), TEXT_XML("text/xml"),

	VIDEO_AVI("video/avi"), VIDEO_EXAMPLE("video/example"), VIDEO_MPEG("video/mpeg"), VIDEO_MP4("video/mp4"), VIDEO_OGG(
			"video/ogg"), VIDEO_QUICKTIME("video/quicktime"), VIDEO_WEBM("video/webm"), VIDEO_MATROSKA(
					"video/x-matroska"), VIDEO_X_MS("video/x-ms-wmv"), VIDEO_X_FLV("video/x-flv"),

	OTHER("not-defined");

	private final String value;

	Mime(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
