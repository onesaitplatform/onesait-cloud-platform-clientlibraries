//::///////////////////////////////////////////////////////////////////////////
//:: Onesait Cesium API
//:: onesait_cesium_api_b1-13.js
//:: Copyright (c) 2019 Minsait by Indra.
//::///////////////////////////////////////////////////////////////////////////
/*
	Version: beta 1.13
    
	* findFeatureFromLayerById added to be able to find a feature in a layer
	from its ID.
	
	* updateFeatureFromLayer() added to allow to update the properties of one
	entity from	a defined layer. New properties come from an object, so can update
	several	properties at once. This method is still in development.

*/
//::///////////////////////////////////////////////////////////////////////////
//:: Created By: Francisco Javier Lopez Acevedo (fjlacevedo@minsait.com)
//:: Created On: 2019-04-22
//:: Last Modified By: fjlacevedo
//:: Last Modified On: 2019-07-04
//::///////////////////////////////////////////////////////////////////////////

/** API presentation log */
console.warn('Cesium API loaded and working.\nVersion: beta 1.13')

/** Set the global viewer variable. You can define the viewer properties in your
 * own HTML/javascript file, or in the javascript screen in the template gadget
 * over the dashboard */
let viewer

/** Check if the API is used in the Onesait Platform Dashboard or not */
let onesaitPlatformDashboard =
	window && window.location.pathname.includes('/controlpanel/dashboards/')
		? true
		: false

/** Move the camera to a map element */
const cameraToElement = (zoomType, elementType, elementName) => {
	switch (elementType) {
		case 'dataSource':
			cameraToDataSource(zoomType, elementName)
			break

		case 'entity':
			cameraToEntity(zoomType, elementName)
			break

		case 'primitive':
			cameraToPrimitive(zoomType, elementName)
			break

		case 'raster':
			// TODO: make the cameraToRaster
			break
	}

	/** Function to move the camera towards a dataSource */
	function cameraToDataSource(
		zoomType,
		layerName,
		duration = 3.0,
		maximumHeight = 0,
		heading = 0.0,
		pitch = -90,
		range = 0.0
	) {
		/** Get the dataSource */
		let dataSource = findDataSourceByLayerName(layerName)

		/** If the dataSource exist, then move the camera toward it */
		if (dataSource) {
			/** Check the type of zoom to do */
			if (zoomType === 'fly') {
				/** Move the camera to the position with the flyTo effect */
				viewer.flyTo(dataSource, {
					duration: duration,
					maximumHeight: maximumHeight,
					offset: new Cesium.HeadingPitchRange(
						heading,
						Cesium.Math.toRadians(pitch),
						range
					)
				})
			} else {
				/** Move the camera to the position with the zoomTo effect */
				viewer.zoomTo(
					dataSource,
					new Cesium.HeadingPitchRange(
						heading,
						Cesium.Math.toRadians(pitch),
						range
					)
				)
			}
		} else {
			console.log('No dataSource found with this name.')
		}
	}

	/** Function to move the camera towards a dataSource */
	function cameraToEntity(
		zoomType,
		elementName,
		duration = 3.0,
		maximumHeight = 0,
		heading = 0.0,
		pitch = -90,
		range = 0.0
	) {
		/** Define the entity */
		let entity

		/** Find the entity through the dataSources */
		if (viewer.dataSources._dataSources.length > 0) {
			for (let viewerDataSource of viewer.dataSources._dataSources) {
				for (let dataSourceEntity of viewerDataSource.entities.values) {
					if (
						dataSourceEntity.featureProperties.id === elementName ||
						dataSourceEntity.featureProperties.name === elementName
					) {
						entity = dataSourceEntity
					}
				}
			}
		}

		/** If the entity isn't inside a dataSource, try to find it in the viewer */
		if (!entity && viewer.entities.values.length > 0) {
			for (let viewerEntity of viewer.entities.values) {
				if (
					(viewerEntity.id && viewerEntity.id === elementName) ||
					(viewerEntity.name && viewerEntity.name === elementName) ||
					('properties.id' in viewerEntity &&
						viewerEntity.properties.id === elementName) ||
					('properties.name' in viewerEntity &&
						viewerEntity.properties.name === elementName)
				) {
					entity = viewerEntity
				}
			}
		}

		/** If the entity exist, then move the camera toward it */
		if (entity) {
			/** Check the type of zoom to do */
			if (zoomType === 'fly') {
				/** Move the camera to the position with the flyTo effect */
				viewer.flyTo(entity, {
					duration: duration,
					maximumHeight: maximumHeight,
					offset: new Cesium.HeadingPitchRange(
						heading,
						Cesium.Math.toRadians(pitch),
						range
					)
				})
			} else {
				/** Move the camera to the position with the zoomTo effect */
				viewer.zoomTo(
					entity,
					new Cesium.HeadingPitchRange(
						heading,
						Cesium.Math.toRadians(pitch),
						range
					)
				)
			}
		} else {
			console.log('No entity found with this ID or name.')
		}
	}

	function cameraToPrimitive(
		zoomType,
		elementName,
		height = '5000',
		heading = '0',
		pitch = '-90',
		roll = '0'
	) {
		/** Define the primitive */
		let primitive

		/** Find the primitive through the scene primitives */
		if (viewer.scene.primitives._primitives.length > 0) {
			for (let primitiveCollection of viewer.scene.primitives._primitives) {
				for (let scenePrimitive of primitiveCollection._primitives) {
					if (
						'id' in scenePrimitive &&
						('id' in scenePrimitive.id || 'name' in scenePrimitive.id) &&
						(scenePrimitive.id.id === elementName ||
							scenePrimitive.id.name === elementName)
					) {
						primitive = scenePrimitive
					}
				}
			}
		}

		/** If the primitive exist, then move the camera toward it */
		if (primitive) {
			/** Get the longitude and latitude values from the primitive id property */
			let longitude
			let latitude

			/** Check if coordinates are defined in the primitive id property */
			if ('coordinates' in primitive.id) {
				longitude = primitive.id.coordinates.longitude
				latitude = primitive.id.coordinates.latitude
			}

			if (longitude && latitude) {
				/** Check the type of zoom to do */
				if (zoomType === 'fly') {
					/** Move the camera to the position with the flyTo effect */
					viewer.camera.flyTo({
						destination: Cesium.Cartesian3.fromDegrees(
							longitude,
							latitude,
							height
						),
						orientation: {
							heading: heading,
							pitch: Cesium.Math.toRadians(pitch),
							roll: roll
						}
					})
				} else {
					/** Move the camera to the position with the zoomTo effect */
					viewer.camera.setView({
						destination: Cesium.Cartesian3.fromDegrees(
							longitude,
							latitude,
							height
						),
						orientation: {
							heading: heading,
							pitch: Cesium.Math.toRadians(pitch),
							roll: roll
						}
					})
				}
			} else {
				console.log(
					'Primitive found, but has no coordinates defined on it. Zoom aborted.'
				)
			}
		} else {
			console.log('No primitive found with this ID or name. Zoom aborted.')
		}
	}
}

/** Move the camera to a designated position */
const cameraToPosition = (
	zoomType,
	coordinates,
	height,
	heading,
	pitch,
	roll
) => {
	/** Set the accepted zoom type parameters */
	let acceptedZoomTypes = ['fly', 'zoom']

	/** Check if the input zoom type exist */
	zoomType =
		typeof zoomType !== 'undefined' && acceptedZoomTypes.includes(zoomType)
			? zoomType
			: 'zoom'

	/** Set the type of coordinates to use */
	let typeCoordinates

	/** Check the type of input coordinates and get the type */
	if (typeof zoomType !== 'undefined') {
		/** Check if the coordinates are degrees or cartesian*/
		if (
			Array.isArray(coordinates) &&
			coordinates.length === 2 &&
			checkValidLongitudeLatitude(coordinates)
		) {
			typeCoordinates = 'degrees'
		} else if (
			typeof coordinates === 'object' &&
			checkValidCartesian3(coordinates)
		) {
			typeCoordinates = 'cartesian'
		}
	}

	/** If method has defined coordinates, resume */
	if (typeCoordinates !== 'undefined') {
		height = typeof height !== 'undefined' && height > 0 ? height : 5000

		heading = typeof heading !== 'undefined' ? normaliseOrientation(heading) : 0

		pitch =
			typeof pitch !== 'undefined' ? evaluatePitchRoll('pitch', pitch) : -90

		roll = typeof roll !== 'undefined' ? evaluatePitchRoll('roll', roll) : 0

		/** Set the zoom variables */
		let destinationConstructor =
			typeCoordinates === 'degrees'
				? Cesium.Cartesian3.fromDegrees(coordinates[0], coordinates[1], height)
				: Cesium.Cartesian3.fromDegrees(
						Cesium.Math.toDegrees(
							Cesium.Ellipsoid.WGS84.cartesianToCartographic(coordinates)
								.longitude
						),
						Cesium.Math.toDegrees(
							Cesium.Ellipsoid.WGS84.cartesianToCartographic(coordinates)
								.latitude
						),
						height
				  )

		let orientationConstructor = {
			heading: Cesium.Math.toRadians(heading),
			pitch: Cesium.Math.toRadians(pitch),
			roll: roll
		}

		let zoomTypeConstructor = zoomType === 'fly' ? 'flyTo' : 'setView'

		/** Make the zoom */
		viewer.camera[zoomTypeConstructor]({
			destination: destinationConstructor,
			orientation: orientationConstructor
		})
	} else {
		console.warn(
			'There is a problem with the coordinates input in ' +
				'cameraToPosition(). Please make sure you are using coordinates as an ' +
				'array of longitude/latitude, or a cartesian object of XYZ.'
		)
	}
}

/** Change the zoom of the camera forward or backward */
const cameraZoom = (zoomDirection, divisor) => {
	/** Check the divisor input */
	if (
		typeof divisor === 'undefined' ||
		typeof divisor !== 'number' ||
		divisor <= 1
	) {
		divisor = 3.0
	}

	/** Set the map ellipsoid */
	let ellipsoid = viewer.scene.globe.ellipsoid

	/** Get the distance of the camera to the surface of the ellipsoid */
	let cameraHeight = ellipsoid.cartesianToCartographic(viewer.camera.position)
		.height

	/** Set a conversion of movement rate from the camera height */
	let movementRate = cameraHeight / divisor

	/** Check if the user used the good strings */
	if (zoomDirection === 'zoomIn' || zoomDirection === 'zoomOut') {
		/** Check the type of zoom */
		if (zoomDirection === 'zoomIn' && cameraHeight >= 100) {
			viewer.camera.moveForward(movementRate)
		} else if (zoomDirection === 'zoomOut') {
			viewer.camera.moveBackward(movementRate)
		}
	} else {
		/** Tell the user that the parameter is not recognized */
		console.error(
			'There used parameter "' +
				zoomDirection +
				'" is not recognized. Please make ' +
				'sure that you use only "zoomIn" or "zoomOut".' +
				'\n\n' +
				'Error catched from: ' +
				'%ccameraZoom()',
			'font-weight: bold'
		)
	}
}

/** Check if the input values are a valid cartesian coordinates */
const checkValidCartesian3 = coordinates => {
	return typeof coordinates !== 'undefined' &&
		typeof coordinates === 'object' &&
		coordinates.hasOwnProperty('x') &&
		!isNaN(coordinates.x) &&
		coordinates.hasOwnProperty('y') &&
		!isNaN(coordinates.y) &&
		coordinates.hasOwnProperty('z') &&
		!isNaN(coordinates.z)
		? true
		: false
}

/** Check if the input data is a correct featureCollection */
const checkValidFeatureCollection = data => {
	/** Check data type and name */
	return data.hasOwnProperty('type') &&
		data.type === 'FeatureCollection' &&
		data.hasOwnProperty('name') &&
		data.name != ''
		? true
		: false
}

/** Check if the input values are a valid longitude and latitude coordinates */
const checkValidLongitudeLatitude = coordinates => {
	/** Set the input values */
	let longitudeValue
	let latitudeValue

	/** Check if the input values are correct, and assign them to it variables */
	if (
		typeof coordinates !== 'undefined' &&
		Array.isArray(coordinates) &&
		coordinates.length === 2 &&
		!isNaN(coordinates[0]) &&
		!isNaN(coordinates[1])
	) {
		longitudeValue = coordinates[0]
		latitudeValue = coordinates[1]
	}

	/** If values exist, continue the checking */
	if (
		typeof longitudeValue !== 'undefined' &&
		typeof latitudeValue !== 'undefined'
	) {
		/** Check the longitude value */
		let longitude =
			typeof longitudeValue === 'number' &&
			-180 <= longitudeValue &&
			longitudeValue <= 180
				? longitudeValue
				: undefined

		/** Check the latitude value */
		let latitude =
			typeof latitudeValue === 'number' &&
			-90 <= latitudeValue &&
			latitudeValue <= 90
				? latitudeValue
				: undefined

		/** Return the result of the check */
		return typeof longitude !== 'undefined' && typeof latitude !== 'undefined'
			? true
			: false
	} else {
		return false
	}
}

/** Create a layer with the following properties */
const createLayer = (
	data,
	featureType,
	geometryType,
	symbologyType,
	symbologyProperties,
	layerVisibility,
	allowPicking,
	allowPopup,
	allowClustering,
	clusteringProperties
) => {
	/** Function that add all properties into an object */
	const addPropertiesFromFeature = feature => {
		/** Check if properties are not empty */
		if (feature.properties !== null) {
			/** Get the content of the object */
			let propertyEntry = Object.entries(feature.properties)

			/** Set an object where load the object properties */
			let propertyObject = {}

			/** Iterate over the content object */
			propertyEntry.forEach(entry => {
				/** Filter the content of the properties, removing those ones that are
				 * not useful, like the billboards URLs if exist */
				if (
					entry[0] != 'id' &&
					entry[0] != 'name' &&
					entry[0] != 'billboardDefault' &&
					entry[0] != 'billboardSelected' &&
					entry[0] != 'billboardScale'
				) {
					/** Generate the key and value of the object */
					propertyObject[entry[0]] = entry[1]
				}
			})

			/** Return the object */
			return propertyObject
		}
	}

	/** RUNNING */

	if (featureType === 'entity' || featureType === 'primitive') {
		/** Check if the data comes in a FeatureCollection format */

		if (checkValidFeatureCollection(data)) {
			/** Get the name of the layer */
			let layerName = data.name

			/** Check the feature type */
			if (featureType === 'entity' || featureType === 'primitive') {
				/** Define the dataSource and dataSource name variables */
				let dataSource
				let dataSourceName

				/** Check the feature type, and if is an entity, settle the dataSource */
				if (featureType === 'entity') {
					/** Set the dataSource name */
					dataSourceName = generateDataSourceNameFromLayerName(layerName)

					/** Check if a dataSource with this name exists */
					dataSource = findDataSourceTypeByName(dataSourceName, featureType)

					/** If the dataSource has not been found, then create a new one */
					if (!dataSource) {
						/** Create the new custom dataSource */
						dataSource = new Cesium.CustomDataSource({
							name: dataSourceName,
							layerName: layerName
						})

						/** Set the dataSource visibility option */
						dataSource.show = layerVisibility === 'hideLayer' ? false : true
						dataSource.dataSourceProperties = {}
						dataSource.dataSourceProperties.visibility =
							layerVisibility === 'hideLayer' ? 'hide' : 'show'

						/** Add the dataSource to the viewer */
						viewer.dataSources.add(dataSource)
					}
				} else if (featureType === 'primitive') {
					/** Set the dataSource name */
					let dataSourceName = generateDataSourceNameFromLayerName(layerName)

					/** Check if a dataSource with this name exists */
					dataSource = findDataSourceTypeByName(dataSourceName, featureType)

					/** If the dataSource (primitive collection) has not been found, then
					 * create a new one */
					if (!dataSource) {
						/** Create a new custom dataSource */
						dataSource = new Cesium.PointPrimitiveCollection()

						/** Add the name property. First add the object, next settle it */
						dataSource.name = {}
						dataSource.name.name = dataSourceName
						dataSource.name.layerName = layerName

						/** Set the dataSource visibility option */
						dataSource.dataSourceProperties = {}
						dataSource.dataSourceProperties.visibility =
							layerVisibility === 'hideLayer' ? 'hide' : 'show'
						dataSource.show = layerVisibility === 'hideLayer' ? false : true

						/** Add the dataSource to the viewer */
						viewer.scene.primitives.add(dataSource)
					}
				}

				/** Iterate over all entities in the FeatureCollection */
				data.features.forEach(feature => {
					/** Set a default ID and name of each feature */
					let id
					let name

					/** If ID and/or name are declared in the data properties, then
					 * use them */
					if (
						feature.hasOwnProperty('properties') &&
						feature.properties !== null &&
						feature.properties.hasOwnProperty('id') &&
						feature.properties.id != ''
					) {
						/** Set the ID as the value from the feature ID */
						id = feature.properties.id
					}

					if (
						feature.hasOwnProperty('properties') &&
						feature.properties !== null &&
						feature.properties.hasOwnProperty('name') &&
						feature.properties.name != ''
					) {
						/** Set the name as the value from the feature name */
						name = feature.properties.name
					}

					/** Set the feature constructor */
					let featureConstructor = {}

					/** Set the property objects to add to the constructor */
					let featureProperties = {
						allowPicking: allowPicking === 'disablePicking' ? false : true,
						allowPopup: allowPopup === 'enablePopup' ? true : false,
						id: id,
						layerName: layerName,
						name: name,
						featureType: featureType,
						geometryType: {
							classType: geometryType,
							groupType: feature.geometry.type
						},
						parentDataSource: dataSourceName,
						properties: addPropertiesFromFeature(feature)
					}

					/** Create the symbology and properties objects */
					let symbology = {}
					let featureSymbology = {}

					/** Set the symbology and symbology properties */
					switch (geometryType) {
						case 'point':
							/** Check if the symbology is a billboard or a color element */
							if (
								symbologyType === 'billboard' ||
								symbologyType === 'billboardField'
							) {
								/** Check if the selected symbol is autogenerated */
								if (
									(symbologyType === 'billboard' ||
									symbologyType === 'billboardField'
										? symbologyProperties[1]
										: feature.properties[symbologyProperties[1]]) ===
									'fromDefaultBillboard'
								) {
									let billboardDefaultSvg

									/** Check if is NOT a SVG. In this case, get the XML content */
									if (
										!(symbologyType === 'billboard'
											? symbologyProperties[0]
											: feature.properties[symbologyProperties[0]]
										).includes('svg xmlns')
									) {
										/** Get the default billboard as an element */
										billboardDefaultSvg = httpGet(
											'xml',
											symbologyType === 'billboard'
												? symbologyProperties[0]
												: feature.properties[symbologyProperties[0]]
										)
									} else {
										/** Get the default billboard from the property as a string */
										billboardDefaultSvg =
											symbologyType === 'billboard'
												? symbologyProperties[0]
												: feature.properties[symbologyProperties[0]]
									}

									/** Generate the properties object */
									featureSymbology.symbologyType = symbologyType
									featureSymbology.billboardDefault =
										symbologyType === 'billboard'
											? symbologyProperties[0]
											: feature.properties[symbologyProperties[0]]
									featureSymbology.billboardSelected = svgBillboardDefaultToSvgBillboardSelected(
										billboardDefaultSvg
									)

									featureSymbology.billboardScale =
										symbologyType === 'billboard'
											? symbologyProperties[2]
											: feature.properties[symbologyProperties[2]]
								} else {
									/** Generate the properties object */
									featureSymbology.symbologyType = symbologyType
									featureSymbology.billboardDefault =
										symbologyType === 'billboard'
											? symbologyProperties[0]
											: feature.properties[symbologyProperties[0]]
									featureSymbology.billboardSelected =
										symbologyType === 'billboard'
											? symbologyProperties[1]
											: feature.properties[symbologyProperties[1]]
									featureSymbology.billboardScale =
										symbologyType === 'billboard'
											? symbologyProperties[2]
											: feature.properties[symbologyProperties[2]]
								}

								/** Generate the properties object */
								symbology = {
									image: featureSymbology.billboardDefault,
									scale: featureSymbology.billboardScale,
									heightReference: Cesium.HeightReference.CLAMP_TO_GROUND
								}
							} else if (
								symbologyType === 'color' ||
								symbologyType === 'colorField'
							) {
								/** Generate the properties objects */
								featureSymbology.symbologyType = symbologyType
								symbology = {}

								featureSymbology.pixelSize =
									symbologyType === 'color'
										? symbologyProperties[0]
										: feature.properties[symbologyProperties[0]]

								featureSymbology.color =
									symbologyType === 'color'
										? symbologyProperties[1]
										: feature.properties[symbologyProperties[1]]

								featureSymbology.colorAlpha =
									symbologyType === 'color'
										? symbologyProperties[2]
										: feature.properties[symbologyProperties[2]]

								/** Add the properties to the object */
								symbology.pixelSize = featureSymbology.pixelSize
								symbology.color = Cesium.Color.fromCssColorString(
									featureSymbology.color
								).withAlpha(featureSymbology.colorAlpha)

								if (
									typeof symbologyProperties[3] != 'undefined' &&
									typeof symbologyProperties[4] != 'undefined' &&
									typeof symbologyProperties[5] != 'undefined'
								) {
									featureSymbology.outlineWidth =
										symbologyType === 'color'
											? symbologyProperties[3]
											: feature.properties[symbologyProperties[3]]

									featureSymbology.outlineColor =
										symbologyType === 'color'
											? symbologyProperties[4]
											: feature.properties[symbologyProperties[4]]

									featureSymbology.outlineColorAlpha =
										symbologyType === 'color'
											? symbologyProperties[5]
											: feature.properties[symbologyProperties[5]]

									/** Add the properties to the object */
									symbology.outlineWidth = featureSymbology.outlineWidth
									symbology.outlineColor = Cesium.Color.fromCssColorString(
										featureSymbology.outlineColor
									).withAlpha(featureSymbology.outlineColorAlpha)
								}
							}
							break

						case 'lineString':
							/** Add the property of 'polyline' to the feature constructor */
							featureConstructor.polyline = {}

							/** Add the properties to the constructor, filtering if the data
							 * comes from the input parameter or a field */
							featureSymbology.symbologyType = symbologyType
							featureSymbology.width =
								symbologyType === 'color'
									? symbologyProperties[0]
									: feature.properties[symbologyProperties[0]]
							featureSymbology.color =
								symbologyType === 'color'
									? symbologyProperties[1]
									: feature.properties[symbologyProperties[1]]
							featureSymbology.colorAlpha =
								symbologyType === 'color'
									? symbologyProperties[2]
									: feature.properties[symbologyProperties[2]]

							symbology.width = featureSymbology.width
							symbology.material = Cesium.Color.fromCssColorString(
								featureSymbology.color
							).withAlpha(featureSymbology.colorAlpha)
							break

						case 'polygon':
							/** Add the property of 'polygon' to the feature constructor */
							featureConstructor.polygon = {}

							/** Add the properties to the constructor, filtering if the data
							 * comes from the input parameter or a field */
							featureSymbology.symbologyType = symbologyType

							featureSymbology.color =
								symbologyType === 'color'
									? symbologyProperties[0]
									: feature.properties[symbologyProperties[0]]
							featureSymbology.colorAlpha =
								symbologyType === 'color'
									? symbologyProperties[1]
									: feature.properties[symbologyProperties[1]]
							featureSymbology.outlineColor =
								symbologyType === 'color'
									? symbologyProperties[2]
									: feature.properties[symbologyProperties[2]]
							featureSymbology.outlineAlpha =
								symbologyType === 'color'
									? symbologyProperties[3]
									: feature.properties[symbologyProperties[3]]

							symbology.material = Cesium.Color.fromCssColorString(
								featureSymbology.color
							).withAlpha(featureSymbology.colorAlpha)

							/** Check if there're optional properties to add */
							if (
								typeof symbologyProperties[2] != 'undefined' &&
								typeof symbologyProperties[3] != 'undefined'
							) {
								featureSymbology.outlineColor =
									symbologyType === 'color'
										? symbologyProperties[2]
										: feature.properties[symbologyProperties[2]]

								featureSymbology.outlineColorAlpha =
									symbologyType === 'color'
										? symbologyProperties[3]
										: feature.properties[symbologyProperties[3]]

								symbology.outlineColor = Cesium.Color.fromCssColorString(
									featureSymbology.outlineColor
								).withAlpha(featureSymbology.outlineColorAlpha)
							}
							break
					}

					/*************************/
					/** FEATURE CONSTRUCTOR **/
					/*************************/

					/** Define a variable, to add the properties in case is a primitive */
					let primitive

					/** Set the point constructor */
					const pointLayer = () => {
						/** Check in the feature type is an entity */
						if (featureType === 'entity') {
							/** Generate the constructor */
							featureConstructor.featureProperties = featureProperties
							featureConstructor.featureSymbology = featureSymbology

							featureConstructor[
								symbologyType === 'billboard' ||
								symbologyType === 'billboardField'
									? 'billboard'
									: 'point'
							] = symbology

							/** Create the entity */
							dataSource.entities.add(featureConstructor)
						} else if (featureType === 'primitive') {
							featureConstructor.pixelSize = featureSymbology.pixelSize
							featureConstructor.color = Cesium.Color.fromCssColorString(
								featureSymbology.color
							).withAlpha(featureSymbology.colorAlpha)

							if (
								typeof symbologyProperties[3] != 'undefined' &&
								typeof symbologyProperties[4] != 'undefined' &&
								typeof symbologyProperties[5] != 'undefined'
							) {
								featureConstructor.outlineWidth = featureSymbology.outlineWidth
								featureConstructor.outlineColor = Cesium.Color.fromCssColorString(
									featureSymbology.outlineColor
								).withAlpha(featureSymbology.outlineColorAlpha)
							}

							let primitive = dataSource.add(featureConstructor)

							primitive.featureProperties = featureProperties
							primitive.featureSymbology = featureSymbology
						}
					}

					/** Set the lineString constructor */
					const lineStringLayer = () => {
						if (featureType === 'entity') {
							/** Add the color property to the constructor */
							featureConstructor.polyline.material = symbology.material
							featureConstructor.polyline.width = symbology.width

							/** Add the standard properties to the constructor */
							featureConstructor.featureProperties = featureProperties
							featureConstructor.featureSymbology = featureSymbology
						}

						/** Check between entities or primitives */
						featureType === 'entity'
							? dataSource.entities.add(featureConstructor)
							: viewer.scene.primitives.add(
									(primitive = new Cesium.Primitive({
										geometryInstances: new Cesium.GeometryInstance({
											geometry: new Cesium.SimplePolylineGeometry({
												positions: featureConstructor.polyline.positions
											}),
											attributes: {
												color: Cesium.ColorGeometryInstanceAttribute.fromColor(
													symbology.material
												)
											}
										}),
										appearance: new Cesium.PerInstanceColorAppearance({
											flat: true,
											renderState: {
												lineWidth: Math.min(
													symbology.width,
													viewer.scene.maximumAliasedLineWidth
												)
											}
										})
									}))
							  )

						if (featureType === 'primitive') {
							/** Set the properties */
							primitive.featureProperties = {}
							primitive.featureSymbology = {}
							primitive.featureProperties = featureProperties
							primitive.featureSymbology = featureSymbology

							/** Overwrite the picking to false, cause polygon primitives are awful */
							primitive.featureProperties.allowPicking = false
						}
					}

					/** Set the polygon constructor */
					const polygonLayer = () => {
						if (featureType === 'entity') {
							/** Add the color property to the constructor */
							featureConstructor.polygon.material = symbology.material

							if (
								typeof symbologyProperties[2] != 'undefined' &&
								typeof symbologyProperties[3] != 'undefined'
							) {
								featureConstructor.polygon.outline = true
								featureConstructor.polygon.outlineColor = symbology.outlineColor
								featureConstructor.polygon.height = 0
								featureConstructor.polygon.heightReference =
									Cesium.HeightReference.CLAMP_TO_GROUND
							}

							/** Add the standard properties to the constructor */
							featureConstructor.featureProperties = featureProperties
							featureConstructor.featureSymbology = featureSymbology
						}

						/** Check between entities or primitives */
						featureType === 'entity'
							? dataSource.entities.add(featureConstructor)
							: viewer.scene.primitives.add(
									(primitive = new Cesium.Primitive({
										geometryInstances: new Cesium.GeometryInstance({
											geometry: Cesium.PolygonGeometry.fromPositions({
												positions: featureConstructor.polygon.hierarchy,
												vertexFormat:
													Cesium.PerInstanceColorAppearance.VERTEX_FORMAT
											}),
											attributes: {
												color: Cesium.ColorGeometryInstanceAttribute.fromColor(
													symbology.material
												)
											}
										}),
										appearance: new Cesium.PerInstanceColorAppearance({
											closed: true,
											translucent: true
										})
									}))
							  )

						if (featureType === 'primitive') {
							/** Set the properties */
							primitive.featureProperties = {}
							primitive.featureSymbology = {}
							primitive.featureProperties = featureProperties
							primitive.featureSymbology = featureSymbology

							/** Overwrite the picking to false, cause polygon primitives are awful */
							primitive.featureProperties.allowPicking = false
						}
					}

					/** Check if is a single point */
					switch (
						feature.geometry.type.charAt(0).toLowerCase() +
							feature.geometry.type.slice(1)
					) {
						case 'point':
							/** Set the position of the feature */
							let longitude = feature.geometry.coordinates[0]
							let latitude = feature.geometry.coordinates[1]

							/** Set the position properties */
							let positionObject = Cesium.Cartesian3.fromDegrees(
								longitude,
								latitude
							)

							/** Add the position property to the constructor */
							featureConstructor.position = positionObject

							/** Create the point layer */
							pointLayer()

							break

						case 'multiPoint':
							/** Get the array of coordinates from each multipoint points */
							let arrayCoordinates = feature.geometry.coordinates

							/** Iterate over the array of multipoint points */
							arrayCoordinates.forEach(coordinates => {
								/** Set the position of the feature */
								let longitude = coordinates[0]
								let latitude = coordinates[1]

								/** Set the position properties */
								let positionObject = Cesium.Cartesian3.fromDegrees(
									longitude,
									latitude
								)

								/** Add the position property to the constructor */
								featureConstructor.position = positionObject

								/** Create the point layer */
								pointLayer()
							})
							break

						case 'lineString':
							/** Set an array of coordinates for each lineString */
							let arrayLineStringList = []

							/** Iterate over all the coordinates elements, and add them to the
							 * array */
							feature.geometry.coordinates.forEach(arrayLineString => {
								arrayLineStringList.push(arrayLineString[0])
								arrayLineStringList.push(arrayLineString[1])
							})

							/** Add the position property to the constructor */
							featureConstructor.polyline.positions = Cesium.Cartesian3.fromDegreesArray(
								arrayLineStringList
							)

							/** Create the lineString layer */
							lineStringLayer()

							break

						case 'multiLineString':
							/** Iterate over all the coordinates elements, and add them to the
							 * array */
							feature.geometry.coordinates.forEach(arrayMultiPolyline => {
								/** Set an array of coordinates for each multiLineString */
								let arrayMultiLineStringList = []

								arrayMultiPolyline.forEach(arrayPolyline => {
									arrayMultiLineStringList.push(arrayPolyline[0])
									arrayMultiLineStringList.push(arrayPolyline[1])
								})

								/** Add the position property to the constructor */
								featureConstructor.polyline.positions = Cesium.Cartesian3.fromDegreesArray(
									arrayMultiLineStringList
								)

								/** Create the lineString layer */
								lineStringLayer()
							})
							break

						case 'polygon':
							/** Set an array of coordinates for each feature */
							let arrayPolygonList = []

							/** Iterate over every polygon coordinates */
							feature.geometry.coordinates.forEach(arrayPolygon => {
								/** Iterate over every couple of coordinate */
								arrayPolygon.forEach(arraySingle => {
									/** Add the coordinates to the array list */
									arrayPolygonList.push(arraySingle[0])
									arrayPolygonList.push(arraySingle[1])
								})
							})

							/** Add the position property to the constructor */
							featureConstructor.polygon.hierarchy = Cesium.Cartesian3.fromDegreesArray(
								arrayPolygonList
							)

							/** Create the polygon layer */
							polygonLayer()
							break

						case 'multiPolygon':
							/** Iterate over all the coordinates elements, and add them to the
							 * array */
							feature.geometry.coordinates.forEach(arrayMultiPolygon => {
								/** Set an array of coordinates for each multiLineString */
								let arrayMultiPolygonList = []

								/** Iterate over each array of multi polygons */
								arrayMultiPolygon.forEach(arrayPolygon => {
									/** Iterate over each polygon array */
									arrayPolygon.forEach(arraySingle => {
										/** Add the coordinates to the array list */
										arrayMultiPolygonList.push(arraySingle[0])
										arrayMultiPolygonList.push(arraySingle[1])
									})
								})

								/** Add the position property to the constructor */
								featureConstructor.polygon.hierarchy = Cesium.Cartesian3.fromDegreesArray(
									arrayMultiPolygonList
								)

								/** Create the polygon layer */
								polygonLayer()
							})
							break
					}
				})

				/** Translate the clustering input */
				let enableClustering

				allowClustering === 'enableClustering'
					? (enableClustering = true)
					: (enableClustering = false)

				/** Check if clustering is enabled, over a point dataSource, exists input
				 * properties for it, and those aren't empty. If so, enable it */
				if (
					enableClustering &&
					featureType === 'entity' &&
					geometryType === 'point' &&
					clusteringProperties &&
					clusteringProperties != ''
				) {
					/** Set the cluster billboard */
					let clusterBillboard = clusteringProperties[0]

					/** Check if the user wants to change the color automatically */
					if (
						clusteringProperties[4] === 'colorFromBillboard' &&
						(symbologyType === 'billboard' ||
							symbologyType === 'billboardField')
					) {
						/** Get the default billboard of the first entity */
						defaultBillboardSvg = httpGet(
							'xml',
							dataSource.entities.values[0].featureSymbology.billboardDefault
						)

						/** Parse the SVG code */
						let parser = new DOMParser()
						let svgParsed = parser.parseFromString(
							defaultBillboardSvg,
							'text/xml'
						)

						/** Get the color of this entity, and store it */
						let defaultColor = svgParsed
							.getElementsByTagName('circle')[0]
							.getAttribute('fill')

						/** Get the cluster billboard */
						clusterBillboardSvg = httpGet('xml', clusteringProperties[0])

						/** Parse it too */
						svgParsed = parser.parseFromString(clusterBillboardSvg, 'text/xml')

						/** Change the background color */
						svgParsed
							.getElementsByTagName('path')[0]
							.setAttribute('fill', defaultColor)

						/** Get the SVG element and transform it to string */
						let svgPath = new XMLSerializer().serializeToString(
							svgParsed.getElementsByTagName('path')[0]
						)

						/** Get the width and height of the original icon, to duplicate it */
						let svgWidth = svgParsed
							.getElementsByTagName('svg')[0]
							.getAttribute('width')
							.replace('px', '')

						let svgHeight = svgParsed
							.getElementsByTagName('svg')[0]
							.getAttribute('height')
							.replace('px', '')

						/** Generate the new SVG code */
						let svgSerialized =
							'<svg xmlns="http://www.w3.org/2000/svg" width="' +
							svgWidth +
							'px" height="' +
							svgHeight +
							'px">'
						svgSerialized += svgPath
						svgSerialized += '</svg>'

						/** Change the SVG to base64 mode to be accepted by Cesium */
						let svgSerializedBase64 =
							'data:image/svg+xml;base64,' + window.btoa(svgSerialized)

						/** Set the image as the SVG serialized in base 64 */
						clusterBillboard = svgSerializedBase64
					}
					if (
						clusteringProperties[4] === 'colorFromPoint' &&
						(symbologyType === 'color' || symbologyType === 'colorField')
					) {
						/** Get the background color of the point */
						let color = dataSource.entities.values[0].featureSymbology.color

						/** Parse the SVG code */
						let parser = new DOMParser()

						/** Get the cluster billboard */
						clusterBillboardSvg = httpGet('xml', clusteringProperties[0])

						/** Parse it too */
						svgParsed = parser.parseFromString(clusterBillboardSvg, 'text/xml')

						/** Change the background color */
						svgParsed
							.getElementsByTagName('path')[0]
							.setAttribute('fill', color)

						/** Get the SVG element and transform it to string */
						let svgPath = new XMLSerializer().serializeToString(
							svgParsed.getElementsByTagName('path')[0]
						)

						/** Get the width and height of the original icon, to duplicate it */
						let svgWidth = svgParsed
							.getElementsByTagName('svg')[0]
							.getAttribute('width')
							.replace('px', '')

						let svgHeight = svgParsed
							.getElementsByTagName('svg')[0]
							.getAttribute('height')
							.replace('px', '')

						/** Generate the new SVG code */
						let svgSerialized =
							'<svg xmlns="http://www.w3.org/2000/svg" width="' +
							svgWidth +
							'px" height="' +
							svgHeight +
							'px">'
						svgSerialized += svgPath
						svgSerialized += '</svg>'

						/** Change the SVG to base64 mode to be accepted by Cesium */
						let svgSerializedBase64 =
							'data:image/svg+xml;base64,' + window.btoa(svgSerialized)

						/** Set the image as the SVG serialized in base 64 */
						clusterBillboard = svgSerializedBase64
					}

					/** Enable clustering in the dataSource */
					dataSource.clustering.enabled = true
					dataSource.clustering.pixelRange = clusteringProperties[1]
					dataSource.clustering.minimumClusterSize = clusteringProperties[2]
					dataSource.clustering.clusterBillboards = true
					dataSource.clustering.clusterLabels = true
					dataSource.clustering.clusterPoints = true

					let billboardScale =
						symbologyType === 'billboard' || symbologyType === 'billboardField'
							? dataSource.entities.values[0].featureSymbology.billboardScale
							: dataSource.entities.values[0].featureSymbology.pixelSize / 25

					let labelScale =
						symbologyType === 'billboard' || symbologyType === 'billboardField'
							? dataSource.entities.values[0].featureSymbology.billboardScale -
							  0.1
							: dataSource.entities.values[0].featureSymbology.pixelSize / 28.5

					/** Set a listener for clustering */
					dataSource.clustering.clusterEvent.addEventListener(
						(entity, cluster) => {
							/** Billboard properties */
							cluster.billboard.show = true
							cluster.billboard.image = clusterBillboard
							cluster.billboard.scale = billboardScale
							cluster.billboard.id = 'clusterFeature'

							/** Label properties */
							cluster.label.show = true
							cluster.label.text = entity.length.toLocaleString()
							cluster.label.scale = labelScale
							cluster.label.height = 2
							cluster.label.outlineWidth = 3
							cluster.label.eyeOffset = new Cesium.Cartesian3(0, 0, -5)
							cluster.label.showBackground = false

							/** Check the number of clustered items, to apply custom personalization */
							if (entity.length < 10) {
								cluster.label.pixelOffset = new Cesium.Cartesian2(-6, 7)
							} else {
								cluster.label.pixelOffset = new Cesium.Cartesian2(-9, 7)
							}

							/** Check the limit of cluster labeling */
							if (entity.length > clusteringProperties[3]) {
								cluster.label.text = clusteringProperties[3] + '+'
								cluster.label.scale = 0.4
								cluster.label.pixelOffset = new Cesium.Cartesian2(-8, 5)
							}
						}
					)
				}
			}
		}
	} else if (featureType === 'raster') {
		if (geometryType === 'heatmap') {
			/** Check if the data comes in a FeatureCollection format */
			if (checkValidFeatureCollection(data)) {
				/** Method that settle the heatmap properties */
				const setupHeatmap = () => {
					let heatmapSetup = {}

					heatmapSetup.radius =
						symbologyType === 'color' || symbologyType === 'colorField'
							? symbologyProperties[3]
							: data[symbologyProperties].searchRadius

					heatmapSetup.minOpacity =
						symbologyType === 'color' || symbologyType === 'colorField'
							? symbologyProperties[4]
							: data[symbologyProperties].minOpacity

					heatmapSetup.maxOpacity =
						symbologyType === 'color' || symbologyType === 'colorField'
							? symbologyProperties[5]
							: data[symbologyProperties].maxOpacity

					heatmapSetup.blur =
						symbologyType === 'color' || symbologyType === 'colorField'
							? symbologyProperties[6]
							: data[symbologyProperties].blur

					heatmapSetup.gradient = searchRadius =
						symbologyType === 'color' || symbologyType === 'colorField'
							? symbologyProperties[7]
							: data[symbologyProperties].gradient

					//scaleRadius: true,
					heatmapSetup.useEntitiesIfAvailable = true

					/** Return the configuration */
					return heatmapSetup
				}

				/** Method that get the extent of the points in the map */
				const getExtent = () => {
					/** Set the arrays for longitude and latitude values */
					let longitudeArray = []
					let latitudeArray = []

					/** Set the extreme values for each array */
					let highestLatitude
					let lowestLatitude
					let highestLongitude
					let lowestLongitude

					/** Iterate over the features of the FeatureCollection */
					data.features.forEach(feature => {
						/** Add the values of longitude and latitude to their arrays */
						longitudeArray.push(feature.geometry.coordinates[0])
						latitudeArray.push(feature.geometry.coordinates[1])
					})

					/** Get the extreme values of the longitude and latitude arrays */
					highestLongitude = Math.max(...longitudeArray)
					lowestLongitude = Math.min(...longitudeArray)
					highestLatitude = Math.max(...latitudeArray)
					lowestLatitude = Math.min(...latitudeArray)

					/** Set the extent of the data */
					let extent = {
						west: lowestLongitude,
						south: lowestLatitude,
						east: highestLongitude,
						north: highestLatitude
					}

					/** Return the extent */
					return extent
				}

				/** Method that get the weight assigned to each point */
				const getWeights = () => {
					/** Set a variable to storage the weight value */
					let weight

					/** Set an array that will content the position plus weight */
					let dataArray = []

					/** Iterate over the features of the FeatureCollection */
					data.features.forEach(feature => {
						/** Look for the weight field through the object objects */
						weight =
							feature.properties[
								symbologyType === 'color' || symbologyType === 'colorField'
									? symbologyProperties[0]
									: symbologyProperties.weightHeatMap
							]

						/** If no weight has been assigned, add a value of 1 */
						if (!weight) {
							weight = 1
						}

						/** Add the position and weight to the data array */
						dataArray.push({
							x: feature.geometry.coordinates[0],
							y: feature.geometry.coordinates[1],
							value: weight
						})
					})

					/** Return the dataArray with the positions and weight */
					return dataArray
				}

				/** Define the heatmap */
				let extent = getExtent()
				let heatMap = CesiumHeatmap.create(viewer, extent, setupHeatmap())

				/** Add the heatmap to the map */
				heatMap.setWGS84Data(
					symbologyType === 'color' || symbologyType === 'colorField'
						? symbologyProperties[1]
						: symbologyProperties.minIterationValue,
					symbologyType === 'color' || symbologyType === 'colorField'
						? symbologyProperties[2]
						: symbologyProperties.maxIterationValue,
					getWeights()
				)

				/** Set the variable of the imagery */
				let imagery

				/** Transform the heatmap as a imagery layer */
				viewer.scene.imageryLayers.addImageryProvider(
					(imagery = new Cesium.SingleTileImageryProvider({
						url: document
							.getElementById(heatMap._id + '-hm')
							.toDataURL('image/webp'),
						rectangle: Cesium.Rectangle.fromRadians(
							heatMap._rectangle.west,
							heatMap._rectangle.south,
							heatMap._rectangle.east,
							heatMap._rectangle.north
						)
					}))
				)

				/** Remove the heatmap entity */
				viewer.entities.remove(heatMap._layer)

				/** Set the dataSource object properties */
				imagery.name = {}
				imagery.name.name = generateDataSourceNameFromLayerName(
					data.name + 'HeatMap'
				)
				imagery.name.layerName = data.name

				let featureProperties = {
					allowPicking: false,
					allowPopup: false,
					layerName: data.name,
					featureType: featureType,
					geometryType: {
						classType: geometryType,
						groupType: 'heatmap'
					},
					parentDataSource: generateDataSourceNameFromLayerName(
						data.name + 'HeatMap'
					)
				}

				/** Set the object properties */
				imagery.featureProperties = {}
				imagery.featureProperties = featureProperties

				imagery.dataSourceProperties = {}
				imagery.dataSourceProperties.visibility =
					layerVisibility === 'hideLayer' ? 'hide' : 'show'

				if (viewer.imageryLayers.length > 0) {
					viewer.imageryLayers._layers.forEach(imageryLayer => {
						if (
							imageryLayer.imageryProvider.hasOwnProperty(
								'featureProperties'
							) &&
							imageryLayer.imageryProvider.featureProperties
								.parentDataSource === imagery.name.name
						) {
							layerVisibility === 'hideLayer'
								? (imageryLayer.show = false)
								: (imageryLayer.show = true)
						}
					})
				}

				/** Remove the DIV */
				document
					.getElementById(heatMap._id)
					.parentNode.removeChild(document.getElementById(heatMap._id))
			}
		} else if (geometryType === 'imagery') {
			let layerName = symbologyType
			let dataSourceName = generateDataSourceNameFromLayerName(layerName)
			let imagery

			/** Add a image to the imagery layers */
			viewer.scene.imageryLayers.addImageryProvider(
				(imagery = new Cesium.SingleTileImageryProvider({
					url: data,
					rectangle: Cesium.Rectangle.fromDegrees(
						symbologyProperties[0],
						symbologyProperties[1],
						symbologyProperties[2],
						symbologyProperties[3]
					)
				}))
			)

			/** Set the imagery properties */
			imagery.name = {}
			imagery.name.layerName = layerName
			imagery.name.name = dataSourceName
		}
	}
}

/** Control map behaviour */
const disableDoubleClickEntityFocus = () => {
	viewer.screenSpaceEventHandler.removeInputAction(
		Cesium.ScreenSpaceEventType.LEFT_DOUBLE_CLICK
	)
}

/** Check the values of pitch and roll */
const evaluatePitchRoll = (checkParam, value) => {
	if (checkParam === 'pitch') {
		let pitch = !isNaN(value)
			? (360 >= value && value >= 270) || (0 >= value && value >= -90)
				? value
				: -90
			: -90
		return pitch
	} else if (checkParam === 'roll') {
		let roll = !isNaN(value) ? (90 >= value && value >= -90 ? value : 0) : 0
		return roll
	}
}

/** Select a feature of the map, changing its color or icon */
const featureSelection = (
	selectionHandler,
	selectionColor,
	selectionColorAlpha,
	cursorAuto
) => {
	/** Method for single feature selection */
	const singleSelection = () => {
		/** Set present selected feature variables */
		let presentSelectedFeature
		let presentFeatureType
		let presentGeometryType
		let presentSymbologyType

		/** Set previous selected feature variables */
		let previousSelectedFeature
		let previousFeatureType
		let previousGeometryType
		let previousSymbologyType

		/** Set an event handler */
		let handler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas)

		/** Get the pick event */
		handler.setInputAction(click => {
			/** Set the pick element */
			let pickedElement = viewer.scene.pick(click.position)

			/** Check if has been clicked over a feature or the background */
			if (pickedElement) {
				/** Check if the picked element is a primitive or an entity */
				if (
					pickedElement.id !== undefined &&
					pickedElement.id.hasOwnProperty('featureProperties') &&
					pickedElement.id.featureProperties.featureType === 'entity' &&
					pickedElement.id.featureProperties.allowPicking
				) {
					/** The actual selected feature will be the viewer selected entity */
					presentSelectedFeature = viewer.selectedEntity
					presentFeatureType = pickedElement.id.featureProperties.featureType
				} else if (
					pickedElement.primitive.hasOwnProperty('featureProperties') &&
					pickedElement.primitive.featureProperties.featureType ===
						'primitive' &&
					pickedElement.primitive.featureProperties.allowPicking
				) {
					/** The actual selected feature will be the the selected primitive */
					presentSelectedFeature = pickedElement.primitive
					presentFeatureType =
						pickedElement.primitive.featureProperties.featureType
				}

				if (presentSelectedFeature) {
					/** Set the actual geometry type from the selected entity */
					presentGeometryType =
						presentSelectedFeature.featureProperties.geometryType.classType

					/** Set the actual symbology type */
					presentSymbologyType =
						presentSelectedFeature.featureSymbology.symbologyType

					/** Change the actual selected feature symbology, checking it type */
					switch (presentGeometryType) {
						case 'point':
							/** Check the present symbology type, considering the feature type */
							if (
								presentSymbologyType === 'billboard' ||
								presentSymbologyType === 'billboardField'
							) {
								presentSelectedFeature.featureSymbology.billboardSelected.includes(
									'svg xmlns'
								)
									? (presentSelectedFeature.billboard.image =
											'data:image/svg+xml;base64,' +
											window.btoa(
												presentSelectedFeature.featureSymbology
													.billboardSelected
											))
									: (presentSelectedFeature.billboard.image =
											presentSelectedFeature.featureSymbology.billboardSelected)
							} else {
								presentFeatureType === 'primitive'
									? (presentSelectedFeature.color = Cesium.Color.fromCssColorString(
											selectionColor
									  ).withAlpha(selectionColorAlpha))
									: (presentSelectedFeature.point.color = Cesium.Color.fromCssColorString(
											selectionColor
									  ).withAlpha(selectionColorAlpha))
							}
							break

						case 'lineString':
							presentFeatureType === 'primitive'
								? (presentSelectedFeature.appearance.material = Cesium.Color.fromCssColorString(
										selectionColor
								  ).withAlpha(selectionColorAlpha))
								: (presentSelectedFeature.polyline.material.color = Cesium.Color.fromCssColorString(
										selectionColor
								  ).withAlpha(selectionColorAlpha))

							break
						case 'polygon':
							presentFeatureType === 'primitive'
								? (presentSelectedFeature.appearance.material = Cesium.Color.fromCssColorString(
										selectionColor
								  ).withAlpha(selectionColorAlpha))
								: (presentSelectedFeature.polygon.material.color = Cesium.Color.fromCssColorString(
										selectionColor
								  ).withAlpha(selectionColorAlpha))
							break
					}

					/** Check if a previous selected feature exists or not */
					if (!previousSelectedFeature) {
						/** Make the actual selection  as the previous selection too */
						previousSelectedFeature = presentSelectedFeature
						previousFeatureType = presentFeatureType
						previousGeometryType = presentGeometryType
						previousSymbologyType = presentSymbologyType
					} else {
						/** Change the previous selected feature symbology, checking it type */
						switch (previousGeometryType) {
							case 'point':
								/** Check the present symbology type, considering the feature type */
								if (
									previousSymbologyType === 'billboard' ||
									previousSymbologyType === 'billboardField'
								) {
									previousSelectedFeature.billboard.image =
										previousSelectedFeature.featureSymbology.billboardDefault
								} else {
									previousFeatureType === 'primitive'
										? (previousSelectedFeature.color = Cesium.Color.fromCssColorString(
												previousSelectedFeature.featureSymbology.color
										  ).withAlpha(
												previousSelectedFeature.featureSymbology.colorAlpha
										  ))
										: (previousSelectedFeature.point.color = Cesium.Color.fromCssColorString(
												previousSelectedFeature.featureSymbology.color
										  ).withAlpha(
												previousSelectedFeature.featureSymbology.colorAlpha
										  ))
								}
								break

							case 'lineString':
								previousFeatureType === 'primitive'
									? (previousSelectedFeature.polyline.material.color = Cesium.Color.fromCssColorString(
											previousSelectedFeature.featureSymbology.color
									  ).withAlpha(
											previousSelectedFeature.featureSymbology.colorAlpha
									  ))
									: (previousSelectedFeature.polyline.material.color = Cesium.Color.fromCssColorString(
											previousSelectedFeature.featureSymbology.color
									  ).withAlpha(
											previousSelectedFeature.featureSymbology.colorAlpha
									  ))

								break
							case 'polygon':
								previousFeatureType === 'primitive'
									? (previousSelectedFeature.polygon.material.color = Cesium.Color.fromCssColorString(
											previousSelectedFeature.featureSymbology.color
									  ).withAlpha(
											previousSelectedFeature.featureSymbology.colorAlpha
									  ))
									: (previousSelectedFeature.polygon.material.color = Cesium.Color.fromCssColorString(
											previousSelectedFeature.featureSymbology.color
									  ).withAlpha(
											previousSelectedFeature.featureSymbology.colorAlpha
									  ))
								break
						}
						/** Make the actual selection  as the previous selection too */
						previousSelectedFeature = presentSelectedFeature
						previousFeatureType = presentFeatureType
						previousGeometryType = presentGeometryType
						previousSymbologyType = presentSymbologyType
					}
				}
			} else {
				if (previousSelectedFeature) {
					/** Change the previous selected feature symbology, checking it type */
					switch (previousGeometryType) {
						case 'point':
							/** Check the present symbology type, considering the feature type */
							if (
								previousSymbologyType === 'billboard' ||
								previousSymbologyType === 'billboardField'
							) {
								previousSelectedFeature.billboard.image =
									previousSelectedFeature.featureSymbology.billboardDefault
							} else {
								previousFeatureType === 'primitive'
									? (previousSelectedFeature.color = Cesium.Color.fromCssColorString(
											previousSelectedFeature.featureSymbology.color
									  ).withAlpha(
											previousSelectedFeature.featureSymbology.colorAlpha
									  ))
									: (previousSelectedFeature.point.color = Cesium.Color.fromCssColorString(
											previousSelectedFeature.featureSymbology.color
									  ).withAlpha(
											previousSelectedFeature.featureSymbology.colorAlpha
									  ))
							}
							break

						case 'lineString':
							previousFeatureType === 'primitive'
								? (previousSelectedFeature.polyline.material.color = Cesium.Color.fromCssColorString(
										previousSelectedFeature.featureSymbology.color
								  ).withAlpha(
										previousSelectedFeature.featureSymbology.colorAlpha
								  ))
								: (previousSelectedFeature.polyline.material.color = Cesium.Color.fromCssColorString(
										previousSelectedFeature.featureSymbology.color
								  ).withAlpha(
										previousSelectedFeature.featureSymbology.colorAlpha
								  ))

							break
						case 'polygon':
							previousFeatureType === 'primitive'
								? (previousSelectedFeature.polygon.material.color = Cesium.Color.fromCssColorString(
										previousSelectedFeature.featureSymbology.color
								  ).withAlpha(
										previousSelectedFeature.featureSymbology.colorAlpha
								  ))
								: (previousSelectedFeature.polygon.material.color = Cesium.Color.fromCssColorString(
										previousSelectedFeature.featureSymbology.color
								  ).withAlpha(
										previousSelectedFeature.featureSymbology.colorAlpha
								  ))
							break
					}
				}

				/** Reset the method variables */
				presentSelectedFeature = undefined
				presentGeometryType = undefined
				presentFeatureType = undefined

				previousSelectedFeature = undefined
				previousGeometryType = undefined
				previousFeatureType = undefined
			}
		}, Cesium.ScreenSpaceEventType.LEFT_CLICK)

		/** Update mouse cursor when hover features */
		handler.setInputAction(function(movement) {
			/** Get the end position after movement */
			let trackedFeature = viewer.scene.pick(movement.endPosition)

			/** Check if tracked element is defined */
			if (Cesium.defined(trackedFeature)) {
				/** Check if the feature allows picking, and change the cursor */
				if (
					(trackedFeature.id &&
						trackedFeature.id.hasOwnProperty('featureProperties') &&
						trackedFeature.id.featureProperties.allowPicking) ||
					(trackedFeature.primitive &&
						trackedFeature.primitive.hasOwnProperty('featureProperties') &&
						trackedFeature.primitive.featureProperties.allowPicking)
				) {
					/** Change the mouse cursor to the pointer one */
					document.body.style.cursor = 'pointer'
				} else {
					/** Check if the user wants the no-drop cursor or not */
					cursorAuto === 'useAutoCursor'
						? (document.body.style.cursor = 'auto')
						: (document.body.style.cursor = 'no-drop')
				}
			} else {
				/** Return the cursor to normal */
				document.body.style.cursor = 'auto'
			}
		}, Cesium.ScreenSpaceEventType.MOUSE_MOVE)
	}

	/** Nothing to see here... yet */
	const multiSelection = () => {}

	switch (selectionHandler) {
		case 'simple':
			singleSelection()
			break

		case 'multiSelection':
			multiSelection()
			break
	}
}

const findDataSourceTypeByName = (dataSourceName, dataSourceType) => {
	/** Set a variable to store the dataSource, if exist*/
	let dataSource

	if (dataSourceType === 'entity') {
		/** Check if at least exist one dataSource */
		if (viewer.dataSources._dataSources.length > 0) {
			/** Iterate over the dataSources */
			viewer.dataSources._dataSources.forEach(viewerDataSource => {
				/** Check if the dataSource has a name as defined */
				if (viewerDataSource.name.name === dataSourceName) {
					dataSource = viewerDataSource
				}
			})
		}
	} else if (dataSourceType === 'primitive') {
		/** Check if at least exist one primitive element */
		if (viewer.scene.primitives.length > 0) {
			/** Iterate over the primitives */
			viewer.scene.primitives._primitives.forEach(viewerDataSource => {
				/** Check if the primitive has a name as defined */
				if (
					viewerDataSource.hasOwnProperty(name) &&
					viewerDataSource.name.name === dataSourceName
				) {
					/** If there's a dataSource (primitive collection) with that name,
					 * reutilize it */
					dataSource = viewerDataSource
				}
			})
		}
	} else if (dataSourceType === 'raster') {
		/** Check if at least exist one raster element */
		if (viewer.scene.imageryLayers._layers.length > 0) {
			/** Iterate over the primitives */
			viewer.scene.imageryLayers._layers.forEach(viewerDataSource => {
				/** Check if the primitive has a name as defined */
				if (
					viewerDataSource.imageryProvider.hasOwnProperty('name') &&
					viewerDataSource.imageryProvider.name.hasOwnProperty('name') &&
					viewerDataSource.imageryProvider.name.name === dataSourceName
				) {
					/** If there's a dataSource (primitive collection) with that name,
					 * reutilize it */
					dataSource = viewerDataSource
				}
			})
		}
	}

	/** If the dataSource is founded, return it */
	if (dataSource) {
		return dataSource
	}
}

/** Get the layer dataSource by it name */
const findDataSourceByLayerName = layerName => {
	let dataSource
	/** Set the name of the dataSource */
	let dataSourceName = generateDataSourceNameFromLayerName(layerName)

	let dataSourcesCount = 0

	/** Begin looking in viewer dataSources */

	/** Check if at least exist one dataSource */
	if (viewer.dataSources._dataSources.length > 0) {
		/** Iterate over the dataSources */
		viewer.dataSources._dataSources.forEach(viewerDataSource => {
			/** Check if the dataSource has a name as defined */
			if (viewerDataSource.name.name === dataSourceName) {
				/** If no dataSource has been found yet, asign it */
				if (!dataSource) {
					dataSource = viewerDataSource
				}
				/** Count the dataSources */
				dataSourcesCount += 1
			}
		})
	}

	/** Check if at least exist one primitive element */
	if (viewer.scene.primitives.length > 0) {
		/** Iterate over the primitives */
		viewer.scene.primitives._primitives.forEach(primitiveDataSource => {
			/** Check if the primitive has a name as defined */
			if (
				primitiveDataSource.hasOwnProperty('name') &&
				primitiveDataSource.name.name === dataSourceName
			) {
				/** If no dataSource has been found yet, asign it */
				if (!dataSource) {
					dataSource = primitiveDataSource
				}
				/** Count the dataSources */
				dataSourcesCount += 1
			}
		})
	}

	/** Check if at least exist one imagery element */
	if (viewer.scene.imageryLayers._layers.length > 0) {
		/** Iterate over the primitives */
		viewer.scene.imageryLayers._layers.forEach(imageryDataSource => {
			/** Check if the primitive has a name as defined */
			if (
				imageryDataSource._imageryProvider.hasOwnProperty('name') &&
				(imageryDataSource._imageryProvider.name.name === dataSourceName ||
					imageryDataSource._imageryProvider.name.name ===
						dataSourceName + 'HeatMap')
			) {
				/** If no dataSource has been found yet, asign it */
				if (!dataSource) {
					dataSource = imageryDataSource
				}
				/** Count the dataSources */
				dataSourcesCount += 1
			}
		})
	}

	if (dataSourcesCount > 1) {
		console.warn(
			dataSourcesCount +
				' dataSources with the same name has been found. ' +
				'The first dataSource found with this name has been returned.'
		)
	}

	if (dataSource) {
		return dataSource
	}
}

/** Find a feature from a layer from its ID */
const findFeatureFromLayerById = (layerName, id) => {
	let returnedFeature

	/** Begin searching in the dataSources */
	if (viewer.dataSources._dataSources.length > 0) {
		viewer.dataSources._dataSources.forEach(dataSource => {
			if (dataSource.name.layerName === layerName) {
				/** Check if the dataSource is not empty */
				if (dataSource.entities.values.length > 0) {
					dataSource.entities.values.forEach(feature => {
						/** Check if the ID/name of the feature exist, and check it against
						 * the input entity */
						if (
							typeof feature.featureProperties.id !== 'undefined' &&
							feature.featureProperties.id == id
						) {
							/** Return  */
							returnedFeature = feature
						}
					})
				}
			}
		})
	}

	/** If the feature has been found */
	if (returnedFeature) {
		return returnedFeature
	}
}

/** FIND VALUE FROM KEY NAME */
function findValueFromKeyName(data, fieldName) {
	/** Check if the field name is not empty */
	if (fieldName != '') {
		/** Look for the field name through the object keys (recursively) */
		Object.keys(data).some(function(keyName) {
			if (keyName === fieldName) {
				fieldValue = data[keyName]
			}

			/** Check if the key is an object or not; in this case, repeat */
			if (data[keyName] && typeof data[keyName] === 'object') {
				/** Return the function, 'cause recursive */
				return findValueFromKeyName(data[keyName], fieldName)
			}
		})
		/** Return the value of the selected field */

		return fieldValue
	}
}

/** Format the layer name to create the dataSource name from it */
const generateDataSourceNameFromLayerName = layerName => {
	/** Set a variable to store the formated layer name */
	let layerNameProcessed

	/** Make the first character as upper case, and join the name if has
	 * several strings */
	let layerNameSplit = layerName.split(/\s/g)

	/** Set a list for the layer name words */
	let arrayNames = []

	/** Iterate over the names to uppercase its first character */
	layerNameSplit.forEach(name => {
		arrayNames.push(name.charAt(0).toUpperCase() + name.slice(1))
	})

	/** Define the layer name from the join */
	layerNameProcessed = arrayNames.join('')

	/** Define the future name of the dataSource */
	let dataSourceName = 'dataSource' + layerNameProcessed

	return dataSourceName
}

/** Get the actual height of the viewer camera */
const getCameraHeight = () => {
	/** Get the position of the camera */
	let cameraPosition = viewer.scene.camera.positionWC

	/** Set the ellipsoid from the camera position */
	let ellipsoid = viewer.scene.globe.ellipsoid.scaleToGeodeticSurface(
		cameraPosition
	)

	/** Set the height from the ellipsoid */
	let height = Cesium.Cartesian3.magnitude(
		Cesium.Cartesian3.subtract(
			cameraPosition,
			ellipsoid,
			new Cesium.Cartesian3()
		)
	)

	/** Return the height  */
	return height ? height : undefined
}

/** Get the actual extent of the viewer canvas */
const getExtent = () => {
	/** Set the canvas position */
	let topLeft = { x: 0, y: 0 }
	let bottomRight = { x: 0, y: 0 }

	/** Get the map canvas */
	let canvas = viewer.scene.canvas

	/** Set the bottom right canvas coordinates */
	bottomRight.x = canvas.width
	bottomRight.y = canvas.height

	/** Transform the canvas coordinates to map coordinates */
	let cartesianTopLeft = viewer.camera.pickEllipsoid(
		topLeft,
		viewer.scene.globe.ellipsoid
	)
	let cartesianBottomRight = viewer.camera.pickEllipsoid(
		bottomRight,
		viewer.scene.globe.ellipsoid
	)

	if (
		typeof cartesianTopLeft !== 'undefined' &&
		typeof cartesianBottomRight !== 'undefined'
	) {
		/** Transform the cartesian coordinates to longitude/latitude coordinates */
		let cartographicTopLeft = Cesium.Cartographic.fromCartesian(
			cartesianTopLeft
		)
		let cartographicBottomRight = Cesium.Cartographic.fromCartesian(
			cartesianBottomRight
		)

		/** Get the longitude and latitude */
		let longitudeTopLeft = Cesium.Math.toDegrees(cartographicTopLeft.longitude)
		let latitudeTopLeft = Cesium.Math.toDegrees(cartographicTopLeft.latitude)

		let longitudeBottomRight = Cesium.Math.toDegrees(
			cartographicBottomRight.longitude
		)
		let latitudeBottomRight = Cesium.Math.toDegrees(
			cartographicBottomRight.latitude
		)

		/** Construct the rectangle object to return */
		return {
			west: latitudeTopLeft,
			south: longitudeBottomRight,
			east: latitudeBottomRight,
			north: longitudeTopLeft
		}
	} else {
		console.warn(
			'Canvas outside the globe in getExtent(). Please make sure ' +
				"you're not trying to get the extent over the universe."
		)
	}
}

/** Change the visibility property of a group of layer depending of it height */
const handleLayerVisibilityByHeight = (
	layerList,
	visibilityOption,
	maxHeight,
	minHeight
) => {
	/** Set a list to store the dataSources to be handled */
	let dataSourcesList = []

	/** Define the accepter inputs for visibility option */
	let acceptedVisibilityOptions = ['show', 'hide']

	/** Iterate over layers, and find it dataSources */
	if (typeof layerList !== 'undefined') {
		if (Array.isArray(layerList)) {
			layerList.forEach(layerName => {
				/** Get the dataSource from the layer name */
				let dataSource = findDataSourceByLayerName(layerName)

				/** If the dataSource has been found, add it to the list */
				if (dataSource) {
					dataSourcesList.push(dataSource)
				}
			})
		} else if (typeof layerList === 'string') {
			/** Get the dataSource from the layer name */
			let dataSource = findDataSourceByLayerName(layerList)

			/** If the dataSource has been found, add it to the list */
			if (dataSource) {
				dataSourcesList.push(dataSource)
			}
		}
	}

	/** Check if layerList has at least one dataSource, and max height is right */
	if (
		typeof maxHeight !== 'undefined' &&
		!isNaN(maxHeight) &&
		dataSourcesList.length > 0
	) {
		/** Check the min height and rewrite it content if necessary */
		minHeight =
			typeof minHeight !== 'undefined' &&
			!isNaN(minHeight) &&
			minHeight < maxHeight
				? minHeight
				: 0

		/** Check if visibility option exist, and has an accepted input value */
		visibilityOption =
			typeof visibilityOption !== 'undefined' &&
			acceptedVisibilityOptions.includes(visibilityOption)
				? visibilityOption
				: 'show'

		/** Transform the input to a boolean value */
		if (visibilityOption === 'show') {
			visibilityOption = true
		} else if (visibilityOption === 'hide') {
			visibilityOption = false
		}

		/** Set a variable to store the height */
		let height

		/** Set the percentage of camera change to listen */
		viewer.camera.percentageChanged = 0.001

		/** Set up a listener of the camera movement */
		viewer.camera.changed.addEventListener(() => {
			/** Get the height */
			height = getCameraHeight()

			/** Iterate over each layer */
			dataSourcesList.forEach(layer => {
				if (layer.dataSourceProperties.visibility === 'show') {
					/** Check the requeriments, and apply the chosen visibility option */
					layer.show =
						minHeight <= height && height <= maxHeight
							? visibilityOption
							: !visibilityOption
				}
			})
		})
	}
}

/** Method to get a XML content from */
const httpGet = (type, url) => {
	{
		if (type === 'xml') {
			/** Set the XML HTTP request */
			let xmlHttp = new XMLHttpRequest()

			/** Get a GET to th URL */
			xmlHttp.open('GET', url, false)
			xmlHttp.send(null)

			/** Return the content */
			return xmlHttp.responseText
		} else {
			console.error('Type unknown, at httpGet()')
		}
	}
}

/** Modify layer visibility with a checkbox in the map */
const layerVisibilitySelectorCheckbox = (layerName, checkBoxId) => {
	/** Set the dataSource */
	let dataSource = findDataSourceByLayerName(layerName)

	/** If the dataSource has been found, resume */
	if (dataSource) {
		if (document.getElementById(checkBoxId).checked) {
			dataSource.show = true
			dataSource.dataSourceProperties.visibility = 'show'
			/** Temporal solution */
			cameraZoom('zoomIn', 500)
			setTimeout(function() {
				cameraZoom('zoomOut', 500)
			}, 10)
		} else {
			dataSource.show = false
			dataSource.dataSourceProperties.visibility = 'hide'
		}
	} else {
		console.warn(
			"Layer don't found. The layer with the name " +
				layerName +
				" hasn't been found in layerVisibilitySelectorCheckbox()"
		)
	}
}

/** Block or unblock the behavior of the camera */
const lockCamera = status => {
	/** Check the input parameter */
	if (status === 'lock' || status === 'unlock') {
		let boolean

		/** Check if lock will be on or off */
		status === 'lock' ? (boolean = false) : (boolean = true)

		/** Apply behavior */
		viewer.scene.screenSpaceCameraController.enableLook = boolean
		viewer.scene.screenSpaceCameraController.enableRotate = boolean
		viewer.scene.screenSpaceCameraController.enableTilt = boolean
		viewer.scene.screenSpaceCameraController.enableTranslate = boolean
		viewer.scene.screenSpaceCameraController.enableZoom = boolean
	} else if (!status) {
		/** Tell the user to use an input */
		console.error(
			'Input parameter empty. Please put a string input in the method. That ' +
				'one must be "lock" or "unlock". Check the API documentation ' +
				'for more information.' +
				'\n\n' +
				'Error catched from: ' +
				'%clockCamera()',
			'font-weight: bold'
		)
	} else {
		/** Tell the user to use the proper string inputs */
		console.error(
			'Input parameter unknown. Please make sure that you use only the ' +
				'strings of "lock" or "unlock" as input parameters of the method. ' +
				'Check the API documentation for more information.' +
				'\n\n' +
				'Error catched from: ' +
				'%clockCamera()',
			'font-weight: bold'
		)
	}
}

/** Normalise orientation between 0 and 360 degrees */
const normaliseOrientation = headingValue => {
	/** Check if the input is a valid number */
	if (!isNaN(headingValue) && headingValue <= 1e5 && headingValue >= -1e5) {
		/** Set the var to store the heading */
		let heading

		/** Check if the input value is over zero */
		if (headingValue >= 0) {
			/** If the value is less than 360, use it as heading */
			if (headingValue <= 360) {
				heading = headingValue
			} else if (headingValue > 360) {
				heading = headingValue
				/** Subtract 360 until get a value less than 361 */
				while (heading > 360) {
					heading = heading - 360
				}
			}
		} else if (headingValue < 0) {
			if (-360 < headingValue) {
				/** If the value is more than -360, take it, adding up 360 to convert
				 * it to positive value */
				heading = 360 + headingValue
			} else if (-360 >= headingValue) {
				/** Set the heading as the input value */
				heading = headingValue

				/** Get the number of times 360 will be subtracted to the heading */
				let divider = Math.floor(heading / 360) + 1
				let multipler = divider * 360

				/** Subtract the value to the heading */
				heading = heading - multipler

				/* Add up 360 to convert it to positive value */
				heading = heading + 360
			}
		}

		/** Return the heading normalised */
		return heading
	} else {
		/** Send a warning about the input value */
		console.warn(
			'The input value used with the method normaliseOrientation() can not be ' +
				'successfully normalised. Please make sure you used an ordinary ' +
				'number as an input.'
		)
		/** Return 0 degrees */
		return 0
	}
}

/** Generate a popup over a feature, showing it properties */
const popup = (selectorHandler, showType, showZoomOption) => {
	/** Check the type of cesium popup to use */
	switch (selectorHandler) {
		case 'single':
			if (onesaitPlatformDashboard) {
				setTimeout(() => {
					createDivElementSinglePopup()
					singlePopup()
				}, 0)
			} else {
				document.addEventListener('DOMContentLoaded', () => {
					createDivElementSinglePopup()
					singlePopup()
				})
			}
			break

		case 'multiple':
			multiplePopup()
			break
	}

	/** Define the popup */
	let popup

	/** Set the longitude and latitude variables */
	let zoomToLongitude
	let zoomToLatitude

	/** Set the selected element */
	let selectedFeature
	let previousSelectedFeature

	/** This will create the HTML containers for the single popup */
	const createDivElementSinglePopup = () => {
		/** Create the HTML DIV and set it ID */
		let popupDiv = document.createElement('div')
		popupDiv.setAttribute('id', 'popup')

		/** Create the other DIVs */

		/** HEADER */
		let popupHeader = document.createElement('div')
		popupHeader.setAttribute('class', 'popup-header')

		let popupHeaderCounter = document.createElement('div')
		popupHeaderCounter.setAttribute('class', 'popup-header-counter')

		let popupHeaderButtons = document.createElement('div')
		popupHeaderButtons.setAttribute('class', 'popup-header-buttons')

		let popupHeaderButtonClose = document.createElement('div')
		popupHeaderButtonClose.setAttribute('class', 'popup-header-buttons-close')

		/** BODY */
		let popupBody = document.createElement('div')
		popupBody.setAttribute('class', 'popup-body')

		let popupBodyTitle = document.createElement('div')
		popupBodyTitle.setAttribute('class', 'popup-body-title')

		let popupBodySeparatorTitleContent = document.createElement('div')
		popupBodySeparatorTitleContent.setAttribute(
			'class',
			'popup-body-separator-title-content'
		)

		let popupBodyContent = document.createElement('div')
		popupBodyContent.setAttribute('class', 'popup-body-content')

		/** FOOTER */
		let popupFooter = document.createElement('div')
		popupFooter.setAttribute('class', 'popup-footer')

		let popupFooterContent = document.createElement('div')
		popupFooterContent.setAttribute('class', 'popup-footer-content')

		/** Get the Cesium DIV */
		let cesiumDiv = document.getElementById('cesiumContainer')

		/** Add the popup DIV after the cesium container div */
		//document.body.insertBefore(popupDiv, cesiumDiv)
		cesiumDiv.parentNode.insertBefore(popupDiv, cesiumDiv)

		/** Add the classes to the DIV */

		/** HEADER */
		document.getElementById('popup').appendChild(popupHeader)
		document
			.getElementById('popup')
			.getElementsByClassName('popup-header')[0]
			.appendChild(popupHeaderCounter)
		document
			.getElementById('popup')
			.getElementsByClassName('popup-header')[0]
			.appendChild(popupHeaderButtons)
		document
			.getElementById('popup')
			.getElementsByClassName('popup-header')[0]
			.getElementsByClassName('popup-header-buttons')[0]
			.appendChild(popupHeaderButtonClose)

		/** BODY */
		document.getElementById('popup').appendChild(popupBody)
		document
			.getElementById('popup')
			.getElementsByClassName('popup-body')[0]
			.appendChild(popupBodyTitle)
		document
			.getElementById('popup')
			.getElementsByClassName('popup-body')[0]
			.appendChild(popupBodySeparatorTitleContent)
		document
			.getElementById('popup')
			.getElementsByClassName('popup-body')[0]
			.appendChild(popupBodyContent)

		/** FOOTER */
		document.getElementById('popup').appendChild(popupFooter)

		document
			.getElementById('popup')
			.getElementsByClassName('popup-footer')[0]
			.appendChild(popupFooterContent)

		/** Settle the popup header */
		document.getElementsByClassName('popup-header-buttons-close')[0].innerHTML =
			'<span class="popup-header-buttons-close">&times;</span>'

		document
			.getElementsByClassName('popup-header-buttons-close')[0]
			.addEventListener('click', closePopup)

		/** Settle the footer content */
		if (showZoomOption === 'showZoom') {
			document.getElementsByClassName('popup-footer-content')[0].innerHTML =
				'Zoom To'

			document
				.getElementsByClassName('popup-footer-content')[0]
				.addEventListener('click', popupFlyTo)
		}
	}

	/** Fly to the picked feature */
	const popupFlyTo = () => {
		/** Invoke the camera to position method */
		cameraToPosition('fly', [zoomToLongitude, zoomToLatitude], 500)
	}

	/** Close popup when click the 'X' symbol */
	const closePopup = pickedElement => {
		/** Change the visibility of the popup to hide it */
		document.getElementById('popup').style.display = 'none'

		/** Check if somethings is picked, to reset the symbology properly */
		if (pickedElement) {
			resetSymbology(selectedFeature)
		}
	}

	/** Method to reset the symbology after closing a popup using the X */
	const resetSymbology = selectedFeature => {
		if (selectedFeature) {
			/** Set the shortcuts to the selected feature properties */
			let selectedFeatureType = selectedFeature.featureProperties.featureType
			let selectedFeatureGeometryType =
				selectedFeature.featureProperties.geometryType.classType
			let previousSymbologyType = selectedFeature.featureSymbology.symbologyType

			/** Change the previous selected feature symbology, checking it type */
			switch (selectedFeatureGeometryType) {
				case 'point':
					/** Check the present symbology type, considering the feature type */
					if (
						previousSymbologyType === 'billboard' ||
						previousSymbologyType === 'billboardField'
					) {
						selectedFeature.billboard.image =
							selectedFeature.featureSymbology.billboardDefault
					} else {
						selectedFeatureType === 'primitive'
							? (selectedFeature.color = Cesium.Color.fromCssColorString(
									selectedFeature.featureSymbology.color
							  ).withAlpha(selectedFeature.featureSymbology.colorAlpha))
							: (selectedFeature.point.color = Cesium.Color.fromCssColorString(
									selectedFeature.featureSymbology.color
							  ).withAlpha(selectedFeature.featureSymbology.colorAlpha))
					}
					break

				case 'lineString':
					selectedFeatureType === 'primitive'
						? (selectedFeature.polyline.material.color = Cesium.Color.fromCssColorString(
								selectedFeature.featureSymbology.color
						  ).withAlpha(selectedFeature.featureSymbology.colorAlpha))
						: (selectedFeature.polyline.material.color = Cesium.Color.fromCssColorString(
								selectedFeature.featureSymbology.color
						  ).withAlpha(selectedFeature.featureSymbology.colorAlpha))

					break
				case 'polygon':
					selectedFeatureType === 'primitive'
						? (selectedFeature.polygon.material.color = Cesium.Color.fromCssColorString(
								selectedFeature.featureSymbology.color
						  ).withAlpha(selectedFeature.featureSymbology.colorAlpha))
						: (selectedFeature.polygon.material.color = Cesium.Color.fromCssColorString(
								selectedFeature.featureSymbology.color
						  ).withAlpha(selectedFeature.featureSymbology.colorAlpha))
					break
			}
		}
	}

	/** This will load the no-drilling version of the cesium popup */
	const singlePopup = () => {
		/** Set the popup container div */
		popup = document.getElementById('popup')

		/** Check if popup exist, and then execute the code */
		if (popup) {
			/** Set variables to store the selected feature and it properties */
			let selectedFeatureType
			let selectedFeatureGeometryType

			/** Set a variable to store the canvas position */
			let canvasPositionDegrees

			/** Handler defined in the canvas of the map */
			let handler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas)

			/** Get the pick event */
			handler.setInputAction(click => {
				/** Set the pick element */
				let pickedElement = viewer.scene.pick(click.position)

				/** Check if anything has been selected */
				if (pickedElement) {
					/** Check if the picked element is a primitive or an entity */
					if (
						pickedElement.id !== undefined &&
						pickedElement.id.hasOwnProperty('featureProperties') &&
						pickedElement.id.featureProperties.featureType === 'entity' &&
						pickedElement.id.featureProperties.allowPicking &&
						pickedElement.id.featureProperties.allowPopup &&
						Object.keys(pickedElement.id.featureProperties.properties).length >
							0
					) {
						/** Check if the picked element is the same as before, in that case
						 * close the popup and reset the variables */
						if (
							previousSelectedFeature &&
							previousSelectedFeature.id === pickedElement.id.id
						) {
							/** Clear the variables */
							selectedFeature = null
							previousSelectedFeature = null

							/** Close the popup */
							closePopup(pickedElement)
						} else {
							/** The actual selected feature will be the viewer selected entity */
							selectedFeature = viewer.selectedEntity
							previousSelectedFeature = selectedFeature
						}
					} else if (
						pickedElement.primitive.hasOwnProperty('featureProperties') &&
						pickedElement.primitive.featureProperties.featureType ===
							'primitive' &&
						pickedElement.primitive.featureProperties.allowPicking &&
						pickedElement.primitive.featureProperties.allowPopup &&
						Object.keys(pickedElement.primitive.featureProperties.properties)
							.length > 0
					) {
						/** The actual selected feature will be the the selected primitive */
						selectedFeature = pickedElement.primitive
					} else {
						/** Clear the variables */
						selectedFeature = null
						previousSelectedFeature = null

						/** Close the popup */
						closePopup(pickedElement)
					}

					/** Check if a selected feature exists */
					if (selectedFeature) {
						/** Set the geometry type */
						selectedFeatureType = selectedFeature.featureProperties.featureType
						selectedFeatureGeometryType =
							selectedFeature.featureProperties.geometryType.classType

						/** Get the selected feature position, depending of it feature type
						 * and geometry type */
						selectedFeatureGeometryType === 'point'
							? selectedFeatureType === 'primitive'
								? (cartesian = selectedFeature.position)
								: (cartesian = selectedFeature.position._value)
							: (cartesian = viewer.camera.pickEllipsoid(
									click.position,
									viewer.scene.globe.ellipsoid
							  ))

						/** Transform the coordinates to cartographic ones */
						let cartographic = Cesium.Ellipsoid.WGS84.cartesianToCartographic(
							cartesian
						)

						/** The popup will be created in those coordinates */
						canvasPositionDegrees = [
							Cesium.Math.toDegrees(cartographic.longitude),
							Cesium.Math.toDegrees(cartographic.latitude)
						]

						/** Define the canvas position through the piked feature */
						let newCartesian = Cesium.Cartesian3.fromDegrees(
							canvasPositionDegrees[0],
							canvasPositionDegrees[1]
						)
						let canvasPosition = Cesium.SceneTransforms.wgs84ToWindowCoordinates(
							viewer.scene,
							newCartesian
						)

						if (showZoomOption === 'showZoom') {
							zoomToLongitude = canvasPositionDegrees[0]
							zoomToLatitude = canvasPositionDegrees[1]
						}

						/** Show the popup window in the canvas defined position */
						popup.style.display = 'block'
						popup.style.position = 'absolute'
						popup.style.left = canvasPosition.x + 'px'
						popup.style.top = canvasPosition.y + 'px'

						/** Set the popup selected feature title property */
						let title = null

						/** Check if the selected feature has a name defined, and if so,
						 * use it */
						if (selectedFeature.featureProperties.name) {
							title = selectedFeature.featureProperties.name
						}

						/** Set the popup title */
						document.getElementsByClassName(
							'popup-body-title'
						)[0].innerHTML = title

						/** If the title is fulfilled, then add a separator between the title
						 * and the content */
						if (!title) {
							/** Add a blank space between the title and the content */
							document.getElementsByClassName(
								'popup-body-separator-title-content'
							)[0].innerHTML = ''
						} else {
							/** Add a line between the title and the content */
							document.getElementsByClassName(
								'popup-body-separator-title-content'
							)[0].innerHTML = '<hr>'
						}

						/** Set the content of the popup */
						let cesiumPopupContent
						/** Get the content for the popup */
						let showingProperties = Object.entries(
							selectedFeature.featureProperties.properties
						)

						if (showType === 'table') {
							/** Set the structure */
							let headerBeginning = '<table><tbody>'
							let content = ''
							let headerEnding = '</tbody></table>'

							/** Iterate over the properties */
							showingProperties.forEach(property => {
								/** Filter the 'name' property */
								if (property[0] != 'name')
									content +=
										'<tr><th>' +
										property[0].charAt(0).toUpperCase() +
										property[0].slice(1) +
										'</th><td>' +
										property[1] +
										'</td></tr>'
							})

							/** Set the popup description content */
							cesiumPopupContent = headerBeginning + content + headerEnding
						} else if (showType === 'description') {
							/** Iterate over the properties */
							showingProperties.forEach(property => {
								/** If any property is description, get the value */
								if (property[0] === 'description') {
									cesiumPopupContent = property[1]
								}
							})

							/** If no description has been found, set it blank */
							if (!cesiumPopupContent) {
								cesiumPopupContent = null
							}
						}

						/** Update the popup content */
						document.getElementsByClassName(
							'popup-body-content'
						)[0].innerHTML = cesiumPopupContent
					}
				} else {
					/** Clear the variables */
					selectedFeature = null
					previousSelectedFeature = null

					/** Close the popup */
					closePopup(pickedElement)
				}
			}, Cesium.ScreenSpaceEventType.LEFT_CLICK)

			/** Update the popup position over the canvas when moving over the map */
			handler.setInputAction(() => {
				/** Check if canvas position degrees has been settle before */
				if (canvasPositionDegrees) {
					/** Define the new cartesian position from degrees */
					let newCartesian = Cesium.Cartesian3.fromDegrees(
						canvasPositionDegrees[0],
						canvasPositionDegrees[1]
					)

					/** Transform those cartesian coordinates onto canvas coordinates */
					let newcanvasPosition = Cesium.SceneTransforms.wgs84ToWindowCoordinates(
						viewer.scene,
						newCartesian
					)

					/** Update the popup location */
					popup.style.left = newcanvasPosition.x + 'px'
					popup.style.top = newcanvasPosition.y + 'px'
				}
			}, Cesium.ScreenSpaceEventType.MOUSE_MOVE)

			/** Update the popup position over the canvas when moving the camera */
			viewer.camera.changed.addEventListener(() => {
				/** Define the mininum movement of the camera */
				viewer.camera.percentageChanged = 0.001

				/** Check if canvas position degrees has been settle before */
				if (canvasPositionDegrees) {
					/** Define the new cartesian position from degrees */
					let newCartesian = Cesium.Cartesian3.fromDegrees(
						canvasPositionDegrees[0],
						canvasPositionDegrees[1]
					)

					/** Transform those cartesian coordinates onto canvas coordinates */
					let newcanvasPosition = Cesium.SceneTransforms.wgs84ToWindowCoordinates(
						viewer.scene,
						newCartesian
					)

					/** Update the popup location */
					popup.style.left = newcanvasPosition.x + 'px'
					popup.style.top = newcanvasPosition.y + 'px'
				}
			})
		}
	}
}

/** Set the scene behavior of the camera */
const sceneCameraBehavior = sceneMode => {
	/** Check the input parameter */
	if (sceneMode === 'scene2D' || sceneMode === 'scene3D') {
		let boolean

		/** Check if lock will be on or off */
		sceneMode === 'scene2D' ? (boolean = false) : (boolean = true)

		/** Apply behavior */
		viewer.scene.screenSpaceCameraController.enableTilt = boolean
	} else if (!sceneMode) {
		/** Tell the user to use an input */
		console.error(
			'Input parameter empty. Please put a string input in the method. That ' +
				'one must be "scene2D" or "scene3D". Check the API documentation ' +
				'for more information.' +
				'\n\n' +
				'Error catched from: ' +
				'%csceneCameraBehavior()',
			'font-weight: bold'
		)
	} else {
		/** Tell the user to use the proper string inputs */
		console.error(
			'Input parameter unknown. Please make sure that you use only the ' +
				'strings of "scene2D" or "scene3D" as input parameters of the method. ' +
				'Check the API documentation for more information.' +
				'\n\n' +
				'Error catched from: ' +
				'%csceneCameraBehavior()',
			'font-weight: bold'
		)
	}
}

/** Send the value or array of values of provided key or array of keys */
const sendValue = (layerName, propertyName) => {
	/** Check if  used in a Dashboard */
	if (onesaitPlatformDashboard) {
		/** Get the dataSource from the layer name */
		let dataSource = findDataSourceByLayerName(layerName)

		/** If dataSource has been found */
		if (dataSource) {
			/** Set an event handler */
			let handler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas)

			/** Get the pick event */
			handler.setInputAction(click => {
				/** Set the selected feature */
				let presentSelectedFeature
				/** Set the pick element */
				let pickedElement = viewer.scene.pick(click.position)

				/** Check if has been clicked over a feature or the background */
				if (pickedElement) {
					/** Check if the picked element is a primitive or an entity */
					if (
						pickedElement.id !== undefined &&
						pickedElement.id.hasOwnProperty('featureProperties') &&
						pickedElement.id.featureProperties.featureType === 'entity' &&
						pickedElement.id.featureProperties.parentDataSource ===
							dataSource.name.name
					) {
						/** The actual selected feature will be the viewer selected entity */
						presentSelectedFeature = viewer.selectedEntity
					} else if (
						pickedElement.primitive.hasOwnProperty('featureProperties') &&
						pickedElement.primitive.featureProperties.featureType ===
							'primitive' &&
						pickedElement.id.featureProperties.parentDataSource ===
							dataSource.name.name
					) {
						/** The actual selected feature will be the the selected primitive */
						presentSelectedFeature = pickedElement.primitive
					}

					/** If something is really selected */
					if (presentSelectedFeature) {
						/** Set the value to send */
						let valueToSend

						if (Array.isArray(propertyName)) {
							/** Make the value to send an array */
							valueToSend = []

							propertyName.forEach(property => {
								/** Check if the property is from the featureProperties */
								if (
									presentSelectedFeature.featureProperties.hasOwnProperty(
										property
									)
								) {
									valueToSend.push(
										presentSelectedFeature.featureProperties[property]
									)
								} else if (
									/** Check if the property is from the properties of featureProperties */
									presentSelectedFeature.featureProperties.properties.hasOwnProperty(
										property
									)
								) {
									valueToSend.push(
										presentSelectedFeature.featureProperties.properties[
											property
										]
									)
								}
							})
						} else {
							/** Check if the property is from the featureProperties */
							if (
								presentSelectedFeature.featureProperties.hasOwnProperty(
									propertyName
								)
							) {
								valueToSend =
									presentSelectedFeature.featureProperties[propertyName]
							} else if (
								/** Check if the property is from the properties of featureProperties */
								presentSelectedFeature.featureProperties.properties.hasOwnProperty(
									propertyName
								)
							) {
								valueToSend =
									presentSelectedFeature.featureProperties.properties[
										propertyName
									]
							}
						}

						/** If the value exists */
						if (valueToSend) {
							/** Send the filter */
							window.fromMap(valueToSend)
						} else {
							console.warn('No value has been sent from sendValue().')
						}
					}
				}
			}, Cesium.ScreenSpaceEventType.LEFT_CLICK)
		} else {
			console.warn(
				'There is no dataSource with the layer name provided. ' +
					'No value has been sent from sendValue().'
			)
		}
	}
}

/** Check if the cursor click means a deselection or empty one */
const sendNullValue = () => {
	/** Check if  used in a Dashboard */
	if (onesaitPlatformDashboard) {
		/** Set an event handler */
		let handler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas)

		/** Get the pick event */
		handler.setInputAction(click => {
			/** Set the pick element */
			let pickedElement = viewer.scene.pick(click.position)

			/** Check if has been clicked over the background */
			if (!pickedElement) {
				window.fromMap(null)
			}
		}, Cesium.ScreenSpaceEventType.LEFT_CLICK)
	}
}

/** Show a hidden layer by it name */
const showHideLayerByLayerName = (showProperty, layerName) => {
	/** Check if show property is a valid parameter. If not, return a 'showLayer' */
	showProperty =
		typeof showProperty !== 'undefined' &&
		typeof showProperty === 'string' &&
		showProperty !== ''
			? showProperty === 'showLayer' || showProperty === 'hideLayer'
				? showProperty
				: 'showLayer'
			: 'showLayer'

	/** Get the dataSource from it layer name */
	let dataSource = findDataSourceByLayerName(layerName)

	if (dataSource) {
		dataSource.show = showProperty === 'showLayer' ? true : false
	}
}

const showHideLayerByName = (showProperty, layerName) => {
	/** Define the visibility status */
	let visibilityStatus

	/** Get the show status from the parameter */
	showProperty === 'hide'
		? (visibilityStatus = false)
		: (visibilityStatus = true)

	/** Process the layerName to get it 'dataSourceName' */
	let layerNameSplit = layerName.split(/\s/g)
	let arrayNames = []

	/** Iterate over the names to uppercase its first character */
	layerNameSplit.forEach(function(name) {
		arrayNames.push(name.charAt(0).toUpperCase() + name.slice(1))
	})

	/** Define the layer name from the join */
	layerNameProcessed = arrayNames.join('')

	/** Set the dataSource */
	let dataSourceName = 'dataSource' + layerNameProcessed

	/** Check if exist at least one dataSource */
	if (viewer.dataSources._dataSources.length > 0) {
		/** Iterate over the viewer dataSources */
		viewer.dataSources._dataSources.forEach(viewerDataSource => {
			if (viewerDataSource.name.name === dataSourceName) {
				viewerDataSource.show = visibilityStatus
			}
		})
	}

	/** Check if exist at least one primitive group */
	if (viewer.scene.primitives._primitives.length > 0) {
		viewer.scene.primitives._primitives.forEach(primitiveGroup => {
			/** Check if the primitive group has a name */
			if (primitiveGroup.name && primitiveGroup.name.name === dataSourceName) {
				/** Iterate over all the primitives to change it visibility property */
				primitiveGroup._pointPrimitives.forEach(primitive => {
					primitive.show = visibilityStatus
				})
			}
		})
	}

	/** Check if we're talking about viewer entities (aka heatmaps) */
	if (viewer.entities.values.length > 0) {
		/** Iterate over viewer entities */
		viewer.entities.values.forEach(entity => {
			if (entity.featureProperties.parentDataSource === dataSourceName) {
				entity.show = visibilityStatus
			}
		})
	}
}

/** Change the visibility property of the selected layer type */
const showHideLayersByType = (showProperty, layerType) => {
	/** Define the visibility status */
	let visibilityStatus

	/** Get the show status from the parameter */
	showProperty === 'hide'
		? (visibilityStatus = false)
		: (visibilityStatus = true)

	/** Show/hide dataSources */
	const showHideDataSources = visibilityStatus => {
		/** Check if exist at least one dataSource */
		if (viewer.dataSources._dataSources.length > 0) {
			/** Iterate over the viewer dataSources */
			viewer.dataSources._dataSources.forEach(viewerDataSource => {
				/** Change the show property */
				viewerDataSource.show = visibilityStatus
			})
		}
	}

	/** Show/hide entities */
	const showHideEntities = visibilityStatus => {
		/** Check if exist at least one entity group */
		if (viewer.entities.values.length > 0) {
			/** Iterate over the entities */
			viewer.entities.values.forEach(entity => {
				entity.show = visibilityStatus
			})
		}
	}

	/** Show/hide primitives */
	const showHidePrimitives = visibilityStatus => {
		/** Check if exist at least one primitive group */
		if (viewer.scene._primitives._primitives.length > 0) {
			/** Iterate over the group primitives */
			viewer.scene._primitives._primitives.forEach(primitiveDataSource => {
				/** Check the type of primitive geometry */
				if (primitiveDataSource.hasOwnProperty('_pointPrimitives')) {
					/** Iterate over each primitive point */
					primitiveDataSource._pointPrimitives.forEach(primitive => {
						primitive.show = visibilityStatus
					})
				} else if (
					primitiveDataSource.hasOwnProperty('_stringLinePrimitives')
				) {
					/** Iterate over each primitive line */
					primitiveDataSource._stringLinePrimitives.forEach(primitive => {
						primitive.show = visibilityStatus
					})
				} else if (primitiveDataSource.hasOwnProperty('_polygonPrimitives')) {
					/** Iterate over each primitive polygon */
					primitiveDataSource._polygonPrimitives.forEach(primitive => {
						primitive.show = visibilityStatus
					})
				}
			})
		}
	}

	/** Show/hide rasters */
	/** TODO: UPDATE TO IMAGERY */
	const showHideHeatmaps = visibilityStatus => {
		/** Check if exist at least one entity group */
		if (viewer.scene.imageryLayers._layers.length > 0) {
			/** Iterate over the entities */
			viewer.scene.imageryLayers._layers.forEach(heatmap => {
				/** Check if the imagery layer is a heatmap */
				if (
					heatmap.imageryProvider.hasOwnProperty('featureProperties') &&
					heatmap.imageryProvider.featureProperties.geometryType.classType ===
						'heatmap'
				) {
					/** Change the show property */
					heatmap.show = visibilityStatus
				}
			})
		}
	}

	/** Check the layer type to change the visibility */
	switch (layerType) {
		/** Change the visibility of all type of layers */
		case 'all':
			showHideDataSources(visibilityStatus)
			showHideEntities(visibilityStatus)
			showHideHeatmaps(visibilityStatus)
			showHidePrimitives(visibilityStatus)
			break

		/** Change the visibility of dataSources */
		case 'dataSources':
			showHideDataSources(visibilityStatus)
			break

		/** Change the visibility of entities */
		case 'entities':
			showHideEntities(visibilityStatus)
			break

		/** Change the visibility of dataSources */
		case 'heatmaps':
			showHideHeatmaps(visibilityStatus)
			break

		/** Change the visibility of dataSources */
		case 'primitives':
			showHidePrimitives(visibilityStatus)
			break
	}
}

/** Generate the selected billboard from the default one */
const svgBillboardDefaultToSvgBillboardSelected = billboardDefaultSvg => {
	/** Parse the SVG code */
	let parser = new DOMParser()
	let svgParsed = parser.parseFromString(billboardDefaultSvg, 'text/xml')

	/** Change some of the SVG properties. Order in elements is mandatory in the
	 * SVG structure */
	svgParsed.getElementsByTagName('circle')[0].setAttribute('fill', '#FFFFFF')
	svgParsed.getElementsByTagName('circle')[1].setAttribute('stroke', '#000000')
	svgParsed.getElementsByTagName('path')[0].setAttribute('fill', '#000000')

	/** Get the SVG elements and transform them to string */
	let svgWidth = svgParsed
		.getElementsByTagName('svg')[0]
		.getAttribute('width')
		.replace('px', '')

	let svgHeight = svgParsed
		.getElementsByTagName('svg')[0]
		.getAttribute('height')
		.replace('px', '')

	let svgBackground = new XMLSerializer().serializeToString(
		svgParsed.getElementsByTagName('circle')[0]
	)

	let svgPath = new XMLSerializer().serializeToString(
		svgParsed.getElementsByTagName('path')[0]
	)

	let svgOutline = new XMLSerializer().serializeToString(
		svgParsed.getElementsByTagName('circle')[1]
	)

	/** Generate the new SVG code */
	let svgSerialized =
		'<svg xmlns="http://www.w3.org/2000/svg" width="' +
		svgWidth +
		'px" height="' +
		svgHeight +
		'px">'
	svgSerialized += svgBackground
	svgSerialized += svgPath
	svgSerialized += svgOutline
	svgSerialized += '</svg>'

	return svgSerialized
}

/** Update some properties of a layer */
const updateLayer = (
	layerName,
	uploadParam,
	newData,
	joinField,
	symbologyType,
	showUpdateInfo
) => {
	let dataSource = findDataSourceByLayerName(layerName)

	/** This'll update entities position */
	const updatePosition = () => {
		if (dataSource) {
			/** Set the old data (newData is one of the params) */
			let oldData = dataSource.entities.values

			/** Set two list to store the old and new data entities IDs */
			let oldDataEntitiesId = []
			let newDataEntitiesId = []

			/** Set two list to store the IDs of what entities must be erased,
			 * updated or created from scratch */
			let entitiesIdToRemove = []
			let entitiesIdToAdd = []

			/** Count the number of removes, updates and adds */
			let countRemoves = 0
			let countUpdates = 0
			let countAdds = 0

			/** Iterate over the new data and retrieve the IDs */
			newData.features.forEach(feature => {
				if (joinField in feature.properties) {
					newDataEntitiesId.push(feature.properties[joinField])
				}
			})

			/** Iterate over the old data and retrieve the IDs */
			oldData.forEach(entity => {
				if (joinField in entity.featureProperties) {
					oldDataEntitiesId.push(entity.featureProperties[joinField])
				} else if (joinField in entity.featureProperties.properties) {
					oldDataEntitiesId.push(entity.featureProperties.properties[joinField])
				}
			})

			/** ENTITIES TO REMOVE */

			/** Clone the array of old data entities IDs to use it as a list of which
			 * entities will be removed from the dataSource */
			entitiesIdToRemove = Array.from(oldDataEntitiesId)

			/** Iterate over the new elements */
			newDataEntitiesId.forEach(newEntityId => {
				/** Iterate over the old elements */
				entitiesIdToRemove.forEach(oldEntityId => {
					/** Check if the new entity ID is equal to the old one */
					if (newEntityId === oldEntityId) {
						/** If so, get the index of the entity ID of the list */
						let index = entitiesIdToRemove.indexOf(oldEntityId)

						if (entitiesIdToRemove.length > -1) {
							/** Remove the ID from the list */
							entitiesIdToRemove.splice(index, 1)
						}
					}
				})
			})

			/** Count the entities to remove */
			countRemoves = entitiesIdToRemove.length

			/** Iterate the entities of the dataSource, and remove the leftovers */
			oldData.forEach(entity => {
				let entityId

				if (joinField in entity.featureProperties) {
					entityId = entity.featureProperties[joinField]
				} else if (joinField in entity.featureProperties.properties) {
					entityId = entity.featureProperties.properties[joinField]
				}

				entitiesIdToRemove.forEach(entityIdToRemove => {
					if (entityId === entityIdToRemove) {
						dataSource.entities.remove(entity)
					}
				})
			})

			/** ENTITIES TO UPDATE */

			/** Iterate over the old data, and update all the entries */
			oldData.forEach(oldEntity => {
				/** Set the old entity ID */
				let oldEntityId

				/** Find the entity selected ID field, and settle the oldEntityId */
				if (joinField in oldEntity.featureProperties) {
					oldEntityId = oldEntity.featureProperties[joinField]
				} else if (joinField in oldEntity.featureProperties.properties) {
					oldEntityId = oldEntity.featureProperties.properties[joinField]
				}

				/** Iterate over the new data, found the feature with the ID, and
				 * update the old entity position */
				newData.features.forEach(feature => {
					let newEntityId

					if (joinField in feature.properties) {
						newEntityId = feature.properties[joinField]
					}

					if (newEntityId) {
						if (oldEntityId === newEntityId) {
							/** Count the entities to update */
							countUpdates += 1

							/** Update the  */
							oldEntity.position = Cesium.Cartesian3.fromDegrees(
								feature.geometry.coordinates[0],
								feature.geometry.coordinates[1]
							)
						}
					}
				})
			})

			/** ENTITIES TO ADD */

			/** Clone the array of new data entities IDs to use it as a list of which
			 * entities will be added to the dataSource */
			entitiesIdToAdd = Array.from(newDataEntitiesId)

			/** Iterate over the old elements */
			oldDataEntitiesId.forEach(oldEntityId => {
				/** Iterate over the new elements */
				entitiesIdToAdd.forEach(newEntityId => {
					/** Check if the old entity ID is equal to the new one */
					if (newEntityId === oldEntityId) {
						/** If so, get the index of the entity ID of the list */
						let index = entitiesIdToAdd.indexOf(newEntityId)

						if (entitiesIdToAdd.length > -1) {
							/** Remove the ID from the list */
							entitiesIdToAdd.splice(index, 1)
						}
					}
				})
			})

			/** Count the entities to add */
			countAdds = entitiesIdToAdd.length

			/** Generate a JSON Feature Collection template */
			let JsonFeatureCollection = {
				type: 'FeatureCollection',
				name: dataSource.name.layerName,
				features: []
			}

			/** Iterate over the new data features, and retrain the ones to be added */
			newData.features.forEach(feature => {
				let featureId

				if (joinField in feature.properties) {
					featureId = feature.properties[joinField]
				}

				/** Iterate over entities to be added */
				entitiesIdToAdd.forEach(entityId => {
					if (featureId === entityId) {
						/** Generate a feature for each element to add */
						let featureElement = {
							type: 'Feature',
							properties: {},
							geometry: {
								type: 'Point',
								coordinates: [0.0, 0.0]
							}
						}

						/** Settle the properties */
						featureElement.properties = feature.properties

						/** Settle the geometry */
						featureElement.geometry.coordinates[0] =
							feature.geometry.coordinates[0]
						featureElement.geometry.coordinates[1] =
							feature.geometry.coordinates[1]
						JsonFeatureCollection.features.push(featureElement)
					}
				})
			})

			/** Check the symbology type to use one or another createLayer() */
			switch (symbologyType) {
				case 'billboard':
					createLayer(
						JsonFeatureCollection,
						false,
						true,
						'point',
						'entity',
						'billboard',
						dataSource.entities.values[0].featureSymbology.billboardDefault,
						dataSource.entities.values[0].featureSymbology.billboardSelected,
						dataSource.entities.values[0].featureSymbology.billboardScale
					)
					break

				case 'billboardField':
					break
			}

			/** If the param is true, show the update info */
			if (showUpdateInfo) {
				/** Clear previous logs */
				console.clear()

				/** Show the changes info */
				console.log('Removed entities: ' + countRemoves)
				console.log('Updated entities: ' + countUpdates)
				console.log('Added entities: ' + countAdds)
			}
		} else {
			console.error(
				'DataSource with name ' + dataSource + ' has not been founded'
			)
		}
	}

	switch (uploadParam) {
		case 'position':
			updatePosition()
			break

		case 'symbology':
			updateSymbology()
			break
	}
}

/** Update some properties of a featore from a layer */
const updateFeatureFromLayer = (layerName, id, updatedProperties) => {
	/** Set the local methods */
	const methods = {
		updateSymbology: {
			updatePointBillboardSymbology: (updatePointBillboardSymbology = () => {
				/** Check if a new color is defined */
				if (
					typeof updatedProperties.symbology.billboardBackground !==
						'undefined' ||
					typeof updatedProperties.symbology.billboardOutline !== 'undefined' ||
					typeof updatedProperties.symbology.billboardIcon !== 'undefined'
				) {
					/** Get the actual billboard */
					let actualBillboard = feature.billboard.image._value

					/** Check if the billboard is a SVG or a URL */
					if (
						actualBillboard
							.split('.')
							.pop()
							.toLowerCase() === 'svg'
					) {
						/** Get the content of the SVG as XML (parsed) */
						fetch(actualBillboard)
							.then(response => response.text())
							.then(svgParse =>
								new window.DOMParser().parseFromString(svgParse, 'text/xml')
							)
							.then(svg => {
								/** Set the new colors, if exist, or reutilize the old ones */
								let newBackgroundColor =
									typeof updatedProperties.symbology.billboardBackground !==
										'undefined' &&
									typeof updatedProperties.symbology.billboardBackground ===
										'string'
										? updatedProperties.symbology.billboardBackground
										: svg.getElementsByTagName('circle')[0].getAttribute('fill')

								let newOutlineColor =
									typeof updatedProperties.symbology.billboardOutline !==
										'undefined' &&
									typeof updatedProperties.symbology.billboardOutline ===
										'string'
										? updatedProperties.symbology.billboardOutline
										: svg
												.getElementsByTagName('circle')[1]
												.getAttribute('stroke')

								let newIconColor =
									typeof updatedProperties.symbology.billboardIcon !==
										'undefined' &&
									typeof updatedProperties.symbology.billboardIcon === 'string'
										? updatedProperties.symbology.billboardIcon
										: svg.getElementsByTagName('path')[0].getAttribute('fill')

								/** Change some of the SVG properties. Order in elements is mandatory in the
								 * SVG structure */
								svg
									.getElementsByTagName('circle')[0]
									.setAttribute('fill', newBackgroundColor)
								svg
									.getElementsByTagName('circle')[1]
									.setAttribute('stroke', newOutlineColor)
								svg
									.getElementsByTagName('path')[0]
									.setAttribute('fill', newIconColor)

								/** Get the SVG elements and transform them to string */
								let svgWidth = svg
									.getElementsByTagName('svg')[0]
									.getAttribute('width')
									.replace('px', '')

								let svgHeight = svg
									.getElementsByTagName('svg')[0]
									.getAttribute('height')
									.replace('px', '')

								let svgBackground = new XMLSerializer().serializeToString(
									svg.getElementsByTagName('circle')[0]
								)

								let svgPath = new XMLSerializer().serializeToString(
									svg.getElementsByTagName('path')[0]
								)

								let svgOutline = new XMLSerializer().serializeToString(
									svg.getElementsByTagName('circle')[1]
								)

								/** Generate the new SVG code */
								let svgSerialized =
									'<svg xmlns="http://www.w3.org/2000/svg" width="' +
									svgWidth +
									'px" height="' +
									svgHeight +
									'px">'
								svgSerialized += svgBackground
								svgSerialized += svgPath
								svgSerialized += svgOutline
								svgSerialized += '</svg>'

								let svgSerializedBase64 =
									'data:image/svg+xml;base64,' + window.btoa(svgSerialized)

								feature.billboard.image = svgSerializedBase64
								feature.featureSymbology.billboardDefault = svgSerializedBase64
							})
					} else if (
						actualBillboard
							.split('.')
							.pop()
							.toLowerCase() === 'png'
					) {
					} else if (
						actualBillboard.split(',')[0] === 'data:image/svg+xml;base64'
					) {
						let decode = window.atob(actualBillboard.split(',')[1])
						let parser = new DOMParser()
						let svg = parser.parseFromString(decode, 'text/xml')

						/** Set the new colors, if exist, or reutilize the old ones */
						let newBackgroundColor =
							typeof updatedProperties.symbology.billboardBackground !==
								'undefined' &&
							typeof updatedProperties.symbology.billboardBackground ===
								'string'
								? updatedProperties.symbology.billboardBackground
								: svg.getElementsByTagName('circle')[0].getAttribute('fill')

						let newOutlineColor =
							typeof updatedProperties.symbology.billboardOutline !==
								'undefined' &&
							typeof updatedProperties.symbology.billboardOutline === 'string'
								? updatedProperties.symbology.billboardOutline
								: svg.getElementsByTagName('circle')[1].getAttribute('stroke')

						let newIconColor =
							typeof updatedProperties.symbology.billboardIcon !==
								'undefined' &&
							typeof updatedProperties.symbology.billboardIcon === 'string'
								? updatedProperties.symbology.billboardIcon
								: svg.getElementsByTagName('path')[0].getAttribute('fill')

						/** Change some of the SVG properties. Order in elements is mandatory in the
						 * SVG structure */
						svg
							.getElementsByTagName('circle')[0]
							.setAttribute('fill', newBackgroundColor)
						svg
							.getElementsByTagName('circle')[1]
							.setAttribute('stroke', newOutlineColor)
						svg
							.getElementsByTagName('path')[0]
							.setAttribute('fill', newIconColor)

						/** Get the SVG elements and transform them to string */
						let svgWidth = svg
							.getElementsByTagName('svg')[0]
							.getAttribute('width')
							.replace('px', '')

						let svgHeight = svg
							.getElementsByTagName('svg')[0]
							.getAttribute('height')
							.replace('px', '')

						let svgBackground = new XMLSerializer().serializeToString(
							svg.getElementsByTagName('circle')[0]
						)

						let svgPath = new XMLSerializer().serializeToString(
							svg.getElementsByTagName('path')[0]
						)

						let svgOutline = new XMLSerializer().serializeToString(
							svg.getElementsByTagName('circle')[1]
						)

						/** Generate the new SVG code */
						let svgSerialized =
							'<svg xmlns="http://www.w3.org/2000/svg" width="' +
							svgWidth +
							'px" height="' +
							svgHeight +
							'px">'
						svgSerialized += svgBackground
						svgSerialized += svgPath
						svgSerialized += svgOutline
						svgSerialized += '</svg>'

						let svgSerializedBase64 =
							'data:image/svg+xml;base64,' + window.btoa(svgSerialized)

						feature.billboard.image = svgSerializedBase64
						feature.featureSymbology.billboardDefault = svgSerializedBase64
					}
				}
			}),
			updatePolygonColorSymbology: (updatePolygonColorSymbology = () => {
				/** check if alpha is defined for main color */
				let colorAlpha =
					typeof updatedProperties.symbology.colorAlpha !== 'undefined'
						? updatedProperties.symbology.colorAlpha
						: feature.featureSymbology.colorAlpha

				/** check if alpha is defined for main color */
				let outlineColorAlpha =
					typeof updatedProperties.symbology.outlineColorAlpha !== 'undefined'
						? updatedProperties.symbology.outlineColorAlpha
						: feature.featureSymbology.outlineColorAlpha

				/** Update the visual main color */
				feature.polygon.material.color =
					typeof updatedProperties.symbology.color !== 'undefined'
						? Cesium.Color.fromCssColorString(
								updatedProperties.symbology.color
						  ).withAlpha(colorAlpha)
						: feature.polygon.material.color

				/** Update the visual outline color */
				if (typeof updatedProperties.symbology.outlineColor !== 'undefined') {
					feature.polygon.outline = true
					feature.polygon.outlineWidth = 1.0
					feature.polygon.height = 0
					feature.polygon.outlineColor = Cesium.Color.fromCssColorString(
						updatedProperties.symbology.outlineColor
					).withAlpha(outlineColorAlpha)

					feature.featureSymbology.outlineColorAlpha = outlineColorAlpha
				}

				/** Update the symbology properties color */
				feature.featureSymbology.color =
					typeof updatedProperties.symbology.color !== 'undefined'
						? updatedProperties.symbology.color
						: feature.polygon.material.color

				feature.featureSymbology.colorAlpha = colorAlpha
			})
		}
	}

	/** Get the feature to update */
	let feature = findFeatureFromLayerById(layerName, id)

	/** If the feature has been found, proceed  */
	if (feature) {
		/** Set some data shortcuts */
		let geometryType = feature.featureProperties.geometryType.classType
		let symbologyType = feature.featureSymbology.symbologyType

		/** Check if the updated properties has symbology info to update*/
		if (typeof updatedProperties.symbology !== 'undefined') {
			/** Check the type of symbology to update */
			if (symbologyType === 'billboard' || symbologyType === 'billboardField') {
				methods.updateSymbology.updatePointBillboardSymbology()
			} else if (symbologyType === 'color' || symbologyType === 'colorField') {
				if (geometryType.toLowerCase() === 'point') {
				} else if (geometryType.toLowerCase() === 'linestring') {
				} else if (
					geometryType.toLowerCase() === 'polygon' &&
					typeof updatedProperties.symbology.color !== 'undefined'
				) {
					methods.updateSymbology.updatePolygonColorSymbology()
				}
			}
		}
	} else {
		console.warn(
			'No feature has been found with an ID of ' + id + ' in updateFeature().'
		)
	}
}