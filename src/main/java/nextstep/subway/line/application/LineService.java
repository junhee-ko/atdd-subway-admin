package nextstep.subway.line.application;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.section.domain.Section;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.domain.Station;

@Service
@Transactional
public class LineService {
	private static final String LINE_NOT_FOUND_MESSAGE = "id에 해당하는 Line을 찾을 수 없습니다.";
	private final LineRepository lineRepository;

	private final StationService stationService;

	public LineService(LineRepository lineRepository, StationService stationService) {
		this.lineRepository = lineRepository;
		this.stationService = stationService;
	}

	public LineResponse saveLine(LineRequest request) {
		Line line = request.toLine();
		Station upStation = this.stationService.getStation(request.getUpStationId());
		Station downStation = this.stationService.getStation(request.getDownStationId());
		Section section = new Section(line, upStation, downStation, request.getDistance());
		line.addSection(section);
		Line persistLine = lineRepository.save(line);
		return LineResponse.of(persistLine);
	}

	public List<LineResponse> findAllLines() {
		List<Line> lines = lineRepository.findAll();

		return lines.stream()
			.map(LineResponse::of)
			.collect(Collectors.toList());
	}

	public LineResponse getLine(long lineId) {
		Line line = this.lineRepository.findById(lineId)
			.orElseThrow(this.getEntityNotFoundExceptionSupplier());
		return LineResponse.of(line);
	}

	private Supplier<EntityNotFoundException> getEntityNotFoundExceptionSupplier() {
		return () -> new EntityNotFoundException(LINE_NOT_FOUND_MESSAGE);
	}

	public LineResponse updateLine(long lineId, LineRequest lineRequest) {
		Line line = this.lineRepository.findById(lineId).orElseThrow(this.getEntityNotFoundExceptionSupplier());
		line.update(lineRequest.toLine());
		return LineResponse.of(this.lineRepository.save(line));
	}

	public void deleteLine(long lineId) {
		this.lineRepository.findById(lineId).orElseThrow(this.getEntityNotFoundExceptionSupplier());
		this.lineRepository.deleteById(lineId);
	}
}
