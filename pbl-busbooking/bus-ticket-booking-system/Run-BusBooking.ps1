$ErrorActionPreference = "Stop"

$java = Join-Path $PSScriptRoot "src\auth\oracleJdk-27\bin\java.exe"
$classes = Join-Path $PSScriptRoot "out"
$driver = Join-Path $PSScriptRoot "lib\mysql-connector-j-9.6.0.jar"

foreach ($path in @($java, $classes, $driver)) {
    if (-not (Test-Path $path)) {
        throw "Required project file or directory was not found: $path"
    }
}

$securePassword = Read-Host "Enter the MySQL password for root" -AsSecureString
if ($securePassword.Length -eq 0) {
    throw "A MySQL password is required."
}

$previousPassword = $env:BUS_DB_PASSWORD
$passwordWasSet = Test-Path Env:\BUS_DB_PASSWORD

try {
    $env:BUS_DB_PASSWORD = [System.Net.NetworkCredential]::new("", $securePassword).Password
    & $java -cp "$classes;$driver" Main
    if ($LASTEXITCODE -ne 0) {
        throw "The application exited with code $LASTEXITCODE."
    }
}
catch {
    Write-Host $_ -ForegroundColor Red
    Read-Host "Press Enter to close this window"
    exit 1
}
finally {
    if ($passwordWasSet) {
        $env:BUS_DB_PASSWORD = $previousPassword
    }
    else {
        Remove-Item Env:\BUS_DB_PASSWORD -ErrorAction SilentlyContinue
    }
    $securePassword.Dispose()
}
