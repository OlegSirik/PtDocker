package ru.pt.api.dto.policy;

import ru.pt.api.dto.commission.CommissionDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Маппинг между JSON-моделью {@link ru.pt.api.dto.policyv3.PolicyDTO} и доменными типами {@link StdPolicy}.
 */
public final class PolicyDtoMapper {

    private PolicyDtoMapper() {
    }

    public static List<InsuredObject> fromDtoList(List<ru.pt.api.dto.policyv3.InsuredObject> src) {
        if (src == null) {
            return null;
        }
        return src.stream().map(PolicyDtoMapper::fromDto).collect(Collectors.toCollection(ArrayList::new));
    }

    public static List<ru.pt.api.dto.policyv3.InsuredObject> toDtoList(List<InsuredObject> src) {
        if (src == null) {
            return null;
        }
        return src.stream().map(PolicyDtoMapper::toDto).collect(Collectors.toCollection(ArrayList::new));
    }

    public static InsuredObject fromDto(ru.pt.api.dto.policyv3.InsuredObject src) {
        if (src == null) {
            return null;
        }
        InsuredObject dst = new InsuredObject();
        dst.setPackageCode(src.getPackageCode());
        dst.setObjectId(src.getObjectId());
        dst.setSumInsured(src.getSumInsured());
        dst.setIoType(src.getIoType());
        dst.setCovers(fromDtoCoverList(src.getCovers()));
        if (src.getAdditionalAttributes() != null) {
            dst.setAdditionalAttributes(new HashMap<>(src.getAdditionalAttributes()));
        }
        return dst;
    }

    public static ru.pt.api.dto.policyv3.InsuredObject toDto(InsuredObject src) {
        if (src == null) {
            return null;
        }
        ru.pt.api.dto.policyv3.InsuredObject dst = new ru.pt.api.dto.policyv3.InsuredObject();
        dst.setPackageCode(src.getPackageCode());
        dst.setObjectId(src.getObjectId());
        dst.setSumInsured(src.getSumInsured());
        dst.setIoType(src.getIoType());
        dst.setCovers(toDtoCoverList(src.getCovers()));
        if (src.getAdditionalAttributes() != null) {
            dst.setAdditionalAttributes(new HashMap<>(src.getAdditionalAttributes()));
        }
        return dst;
    }

    public static List<Cover> fromDtoCoverList(List<ru.pt.api.dto.policyv3.Cover> src) {
        if (src == null) {
            return null;
        }
        return src.stream().map(PolicyDtoMapper::fromDto).collect(Collectors.toCollection(ArrayList::new));
    }

    public static List<ru.pt.api.dto.policyv3.Cover> toDtoCoverList(List<Cover> src) {
        if (src == null) {
            return null;
        }
        return src.stream().map(PolicyDtoMapper::toDto).collect(Collectors.toCollection(ArrayList::new));
    }

    public static Cover fromDto(ru.pt.api.dto.policyv3.Cover src) {
        if (src == null) {
            return null;
        }
        Cover dst = new Cover();
        dst.setCover(fromDto(src.getCover()));
        dst.setRisk(src.getRisk() != null ? new ArrayList<>(src.getRisk()) : null);
        dst.setStartDate(src.getStartDate());
        dst.setEndDate(src.getEndDate());
        dst.setSumInsured(src.getSumInsured());
        dst.setPremium(src.getPremium());
        dst.setDeductible(fromDto(src.getDeductible()));
        dst.setLimitMin(src.getLimitMin());
        dst.setLimitMax(src.getLimitMax());
        return dst;
    }

    public static ru.pt.api.dto.policyv3.Cover toDto(Cover src) {
        if (src == null) {
            return null;
        }
        ru.pt.api.dto.policyv3.Cover dst = new ru.pt.api.dto.policyv3.Cover();
        dst.setCover(toDto(src.getCover()));
        dst.setRisk(src.getRisk() != null ? new ArrayList<>(src.getRisk()) : null);
        dst.setStartDate(src.getStartDate());
        dst.setEndDate(src.getEndDate());
        dst.setSumInsured(src.getSumInsured());
        dst.setPremium(src.getPremium());
        dst.setDeductible(toDto(src.getDeductible()));
        dst.setLimitMin(src.getLimitMin());
        dst.setLimitMax(src.getLimitMax());
        return dst;
    }

    public static CoverInfo fromDto(ru.pt.api.dto.policyv3.CoverInfo src) {
        if (src == null) {
            return null;
        }
        return new CoverInfo(src.getCode(), src.getOption(), src.getDescription());
    }

    public static ru.pt.api.dto.policyv3.CoverInfo toDto(CoverInfo src) {
        if (src == null) {
            return null;
        }
        return new ru.pt.api.dto.policyv3.CoverInfo(src.getCode(), src.getOption(), src.getDescription());
    }

    public static Deductible fromDto(ru.pt.api.dto.policyv3.Deductible src) {
        if (src == null) {
            return null;
        }
        return new Deductible(src.getId(), src.getText());
    }

    public static ru.pt.api.dto.policyv3.Deductible toDto(Deductible src) {
        if (src == null) {
            return null;
        }
        return new ru.pt.api.dto.policyv3.Deductible(src.getId(), src.getText());
    }

    public static Commission fromDto(CommissionDto src) {
        if (src == null) {
            return null;
        }
        Commission dst = new Commission();
        dst.setRequestedCommissionDiscount(src.getRequestedCommissionDiscount());
        dst.setRequestedCommissionRate(src.getRequestedCommissionRate());
        dst.setAppliedCommissionRate(src.getAppliedCommissionRate());
        dst.setCommissionAmount(src.getCommissionAmount());
        dst.setAgdNumber(src.getAgdNumber());
        return dst;
    }

    public static CommissionDto toDto(Commission src) {
        if (src == null) {
            return null;
        }
        CommissionDto dst = new CommissionDto();
        dst.setRequestedCommissionDiscount(src.getRequestedCommissionDiscount());
        dst.setRequestedCommissionRate(src.getRequestedCommissionRate());
        dst.setAppliedCommissionRate(src.getAppliedCommissionRate());
        dst.setCommissionAmount(src.getCommissionAmount());
        dst.setAgdNumber(src.getAgdNumber());
        return dst;
    }

    public static List<Installment> fromDtoInstallmentList(List<ru.pt.api.dto.policyv3.Installment> src) {
        if (src == null) {
            return null;
        }
        return src.stream().map(PolicyDtoMapper::fromDto).collect(Collectors.toCollection(ArrayList::new));
    }

    public static List<ru.pt.api.dto.policyv3.Installment> toDtoInstallmentList(List<Installment> src) {
        if (src == null) {
            return null;
        }
        return src.stream().map(PolicyDtoMapper::toDto).collect(Collectors.toCollection(ArrayList::new));
    }

    public static Installment fromDto(ru.pt.api.dto.policyv3.Installment src) {
        if (src == null) {
            return null;
        }
        return new Installment(src.installmentNr(), src.dueDate(), src.amount());
    }

    public static ru.pt.api.dto.policyv3.Installment toDto(Installment src) {
        if (src == null) {
            return null;
        }
        return new ru.pt.api.dto.policyv3.Installment(src.installmentNr(), src.dueDate(), src.amount());
    }
}
