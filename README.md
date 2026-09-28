# Digital Wallet App

A Spring Boot digital wallet service for creating wallets, transferring funds atomically, and reviewing an immutable transaction history.

## Features

- Create wallet accounts with an opening USD balance
- Transfer funds between two accounts
- Atomic debit and credit using a database transaction
- Exact monetary values using `BigDecimal`
- Idempotent transfers using a client-provided transaction ID
- Clear validation and error responses
- Immutable transfer events for audit and replay
- Responsive browser dashboard with Overview, New account, Transfer, and Audit trail views
- Local file-based H2 database for development

## Requirements

- Java 17 or newer
- Maven, or the included Maven wrapper

## Run locally

On Windows:

```powershell
./mvnw.cmd spring-boot:run
```

On macOS/Linux:

```bash
./mvnw spring-boot:run
```

Open <http://localhost:8080> in a browser.

The local H2 database is stored under `data/`, which is ignored by Git. Delete that directory to reset local wallet data.

## Run with a load balancer

To run two wallet application instances behind Nginx with shared PostgreSQL storage:

```bash
docker compose up --build
```

Open <http://localhost:8081>. Requests go through Nginx and are distributed between `wallet-1` and `wallet-2`. Both instances use the same PostgreSQL database, and transfer operations lock both wallet rows during the transaction to prevent concurrent balance updates. Set `WALLET_PORT=8080` if port 8080 is available.

Stop the deployment with:

```bash
docker compose down
```

Add `-v` only when you intentionally want to delete the PostgreSQL volume and all stored wallet data.

## API

All API routes use the `/v1/wallet` prefix.

### Create a wallet

```http
POST /v1/wallet/wallets
Content-Type: application/json
```

```json
{
	"accountId": "alice",
	"openingBalance": 500.00
}
```

### Transfer funds

```http
POST /v1/wallet/balance_transfer
Content-Type: application/json
```

```json
{
	"fromAccount": "alice",
	"toAccount": "bob",
	"amount": 25.00,
	"currency": "USD",
	"transactionId": "order-1001"
}
```

Successful response:

```json
{
	"status": "success",
	"transactionId": "order-1001"
}
```

### Read data

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/v1/wallet/wallets` | List wallets |
| `GET` | `/v1/wallet/wallets/{accountId}` | Get one wallet |
| `GET` | `/v1/wallet/transactions` | List transfers |
| `GET` | `/v1/wallet/replay` | Rebuild balances from ledger events |

## Idempotency

`transactionId` is the idempotency key. Use a new value for every new transfer. If a request is retried with the same key and the same payload, the original transaction is returned and balances are not changed again.

Reusing a key with a different sender, receiver, amount, or currency returns `409 Conflict`:

```json
{
	"error": "WALLET_REQUEST_FAILED",
	"message": "transactionId already belongs to a different transfer"
}
```

## Error responses

Errors use a consistent JSON structure:

```json
{
	"error": "WALLET_REQUEST_FAILED",
	"message": "Insufficient funds"
}
```

Common statuses include `400` for invalid input, `404` for missing accounts, `409` for duplicate account or conflicting idempotency key, and `422` for insufficient funds.

## Architecture notes

The application uses Spring Web and Spring Data JPA. H2 is the default for local single-instance development; the Compose deployment uses PostgreSQL as shared storage. Wallet balances are projections, while `WalletEvent` records provide an append-only history used by the replay endpoint. This repository is a development-scale implementation; Kafka, Raft replication, sharding, distributed transaction orchestration, authentication, and production observability are not included.

## Tests

```bash
./mvnw test
```