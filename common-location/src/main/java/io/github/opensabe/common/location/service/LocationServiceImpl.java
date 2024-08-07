package io.github.opensabe.common.location.service;

import io.github.opensabe.common.location.vo.GeoLocationData;
import io.github.opensabe.common.utils.IpUtil;
import io.github.opensabe.common.utils.OptionalUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.stream.Collectors;

@Log4j2
public class LocationServiceImpl implements LocationService {
	private final GeoLocation geoLocation;
	private final IpToLocation ipToLocation;

	public LocationServiceImpl(GeoLocation geoLocation, IpToLocation ipToLocation) {
		this.geoLocation = geoLocation;
		this.ipToLocation = ipToLocation;
	}

	@Override
	public GeoLocationData getGeoLocationData(Double latitude, Double longitude) {
		GeoLocationData nearest = null;
		if (latitude != null && longitude != null) {
			nearest = geoLocation.getNearest(latitude, longitude);
		}
		if (nearest == null) {
			String ip = getIp();
			nearest = ipToLocation.getNearest(ip);
		}
		return nearest;
	}

	private static String getIp() {
		HttpServletRequest request = getRequest();
		if (request == null) {
			return null;
		}
		String value = OptionalUtil.orNull(() -> (String) request.getAttribute("ip"));
		return StringUtils.isNotBlank(value) ? value : IpUtil.getIpFromHeader(request);
	}

	private static HttpServletRequest getRequest() {
		try {
			RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();

			if (requestAttributes instanceof ServletRequestAttributes) {
				ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;

				return servletRequestAttributes.getRequest();
			} else {
				return null;
			}
		} catch (Throwable e) {
			String walk = StackWalker.getInstance().walk(stackFrameStream -> stackFrameStream.limit(5).map(stackFrame -> stackFrame.toStackTraceElement().toString()).collect(Collectors.joining("\n")));
			log.debug("get http request in non-servlet context! {}", walk);
			return null;
		}
	}
}
