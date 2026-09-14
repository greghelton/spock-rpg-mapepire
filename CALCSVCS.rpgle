**FREE
Ctl-Opt NoMain Option(*SrcStmt:*NoDebugIo);

// -----------------------------------------------------------------
// Subprocedure: FormatZip
// Formats a 5-digit zip and 4-digit extension into ZIP+4 format
// -----------------------------------------------------------------
Dcl-Proc FormatZip Export;
  Dcl-Pi FormatZip Char(10);
    pZip5 Char(5) Const;
    pZip4 Char(4) Const;
  End-Pi;

  If pZip4 = *Blanks;
    Return pZip5;
  EndIf;

  Return %Trim(pZip5) + '-' + %Trim(pZip4);
End-Proc;

// -----------------------------------------------------------------
// Subprocedure: CalculateMonthlyPayment
// Computes the monthly payment for a fixed-rate amortized loan:
//   M = P * r / (1 - (1 + r)^-n)
// where r = annualRate / 12 and n = months.
// -----------------------------------------------------------------
Dcl-Proc CalculateMonthlyPayment Export;
  Dcl-Pi CalculateMonthlyPayment Packed(15:2);
    pPrincipal Packed(15:2) Const;
    pAnnualRate Packed(5:4) Const;
    pMonths Int(10) Const;
  End-Pi;

  Dcl-S monthlyRate Packed(9:8);
  Dcl-S factor Packed(31:14); 
  Dcl-S payment Packed(15:2);

  If pMonths <= 0;
    Return 0;
  EndIf;

  monthlyRate = pAnnualRate / 12;

  // If the rate is zero, the payment is simply principal divided by term.
  If monthlyRate = 0;
    Return pPrincipal / pMonths;
  EndIf;

  factor = 1 - ((1 + monthlyRate) ** (-pMonths));

  // Round to cents using a consistent half-up policy.
  payment = %Round((pPrincipal * monthlyRate) / factor : 2);

  Return payment;
End-Proc;
