package com.starrycampus.library.config;

import com.starrycampus.library.entity.Seat;
import com.starrycampus.library.repository.SeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 图书馆座位数据初始化器
 * 首次启动时自动为 1F~3F 每层生成 256 个座位（共 768 座）
 * 座位编号规则：{楼层}F{序号}，如 1F001 ~ 1F256
 */
@Component
public class LibraryDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LibraryDataInitializer.class);
    private static final int SEATS_PER_FLOOR = 256;
    private static final int[] FLOORS = {1, 2, 3};

    private final SeatRepository seatRepository;

    public LibraryDataInitializer(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    @Override
    public void run(String... args) {
        long count = seatRepository.count();
        if (count >= SEATS_PER_FLOOR * FLOORS.length) {
            log.info("座位数据已就绪：当前共 {} 个座位，跳过初始化", count);
            return;
        }

        log.info("检测到座位数据不完整（当前 {} 个），开始自动生成 1F~3F 共 {} 个座位...",
                count, SEATS_PER_FLOOR * FLOORS.length);

        List<Seat> seats = new ArrayList<>(SEATS_PER_FLOOR * FLOORS.length);
        for (int floor : FLOORS) {
            for (int i = 1; i <= SEATS_PER_FLOOR; i++) {
                String seatCode = String.format("%dF%03d", floor, i);
                // 跳过已存在的座位编号
                if (seatRepository.findBySeatCode(seatCode).isPresent()) {
                    continue;
                }
                seats.add(Seat.builder()
                        .seatCode(seatCode)
                        .floorArea(floor + "F")
                        .status("available")
                        .build());
            }
        }

        if (!seats.isEmpty()) {
            seatRepository.saveAll(seats);
            log.info("座位初始化完成：新增 {} 个座位，当前共 {} 个座位",
                    seats.size(), seatRepository.count());
        } else {
            log.info("所有座位编号已存在，无需新增");
        }
    }
}
