# Architecture Overview: Product & Pricing Platform (Business View)

## 1. Purpose

This document explains **how insurance products are defined and priced** in a simple, non-technical way. It is intended for business stakeholders, product owners, and domain experts.

The main goal of the system is to:

* Support many insurance products
* Ensure correct and transparent pricing
* Guarantee that already sold policies never change retroactively

---

## 2. Big Picture (Helicopter View)

The system works by **starting from a universal insurance model** and gradually narrowing it down until we get a concrete, sellable product with fixed pricing rules.

Think of it as:

> **All possible insurance concepts → business area → concrete product → fixed price calculation**

Each step only *restricts* and *clarifies* what is allowed. Nothing is changed backward.

---

## 3. Key Concepts Explained Simply

### 3.1 Universal Insurance Model

This is the **largest possible model** of an insurance contract.

It contains *everything that could ever appear* in any insurance product, such as:

* Policyholder
* Insured persons or objects
* Risks and coverages
* Limits, deductibles, premiums

This model is:

* Not sold to customers
* Not used directly for pricing
* A common foundation for all insurance products

---

### 3.2 Line of Business (LoB)

A Line of Business represents a **type of insurance**, for example:

* Accident insurance
* Property insurance
* Liability insurance

Each LoB:

* Selects which parts of the universal model are relevant
* Defines what kind of customers and insured objects are allowed
* Sets common rules and constants for this business area

At this stage, we still do **not** define concrete products or prices.

---

### 3.3 Product

A Product is a **real insurance offering** that can be sold.

The product:

* Removes unnecessary data fields
* Defines which data must be filled in by the customer
* Adds product-specific coverage attributes
* Defines validation rules (ranges, conditions, dependencies)

From the product definition, the system can automatically create:

* Input forms (screens)
* API / JSON request structure
* Data model for pricing

Important:

> The product defines *what data exists*, not *how the price is calculated*.

---

### 3.4 Pricing (Calculator)

Pricing defines **how the premium is calculated**.

It includes:

* Formulas
* Coefficients
* Constants
* The order of calculation steps

Pricing:

* Uses only data defined by the product
* Cannot add or change data fields
* Can be changed independently from the product

---

### 3.5 Production Version (Freeze)

When a product is released to production:

* Its structure is fixed
* Its pricing logic is fixed
* Its validation rules are fixed

This is called a **freeze**.

Why this is important:

* Already sold policies must never change
* Prices must be reproducible
* Legal and audit requirements must be met

If something needs to change, a **new version** is created.

---

## 4. Business Areas (Bounded Contexts)

### Universal Model

* Owns all insurance concepts
* Changes very rarely

### Line of Business

* Defines insurance types (Accident, Property, etc.)
* Applies business-level restrictions

### Product

* Defines concrete sellable products
* Controls required data and validations

### Pricing

* Calculates premium
* Uses fixed product data

### Policy Lifecycle

* Stores and services policies
* Always uses the version that was active at the time of sale

---

## 5. How Everything Fits Together

```text
Universal Insurance Model
          ↓
   Line of Business
          ↓
        Product
          ↓
        Pricing
          ↓
   Frozen Production Version
```

Each step makes the model **smaller, clearer, and more specific**.

---

## 6. Architecture Decision Record (ADR)

### ADR: Product & Pricing Model

**Decision**
Separate product definition (data and rules) from pricing (formulas and calculations), and freeze both when released.

**Why**

* Business flexibility
* Legal safety
* Transparent and reproducible pricing

**Result**

* Products can evolve safely
* Pricing logic is clear and auditable
* Existing contracts remain stable

---

## 7. Summary

This architecture allows the business to:

* Quickly create new insurance products
* Control complexity
* Ensure pricing correctness
* Meet regulatory and audit requirements

All changes move forward through versioning — nothing breaks existing contracts.

---

## System Context

### Purpose

The Insurance Platform operates as a **core business system** responsible for product configuration, pricing, policy data, and document generation. It is designed to be consumed by multiple sales channels and internal users while relying on several external systems for identity, reference data, and distribution.

This section describes **what systems interact with the platform and how**, without internal technical details.

---

### Primary Actors

#### Business Users

* Product managers configure insurance products and tariffs
* Underwriters define risks, limits, and rules
* Operations users issue policies and generate documents

**Value:** ability to launch and manage products without IT involvement

---

#### Sales Channels

* Internal sales portals
* External partners (brokers, aggregators)
* Embedded sales via partner systems

**Interaction:**

* Submit data for quote and policy creation
* Receive calculated price and documents

---

### External Systems

#### Identity & Access Management (IAM)

* Provides authentication of users and partner systems
* Issues access tokens for API usage
* Manages client credentials for external partners

**Role in context:**

* Confirms *who* is calling the platform
* Platform decides *what* is allowed

---

#### Reference Data / Master Data

* Stores business code lists (classifiers)
* Provides code → name mappings
* May be shared across multiple enterprise systems

**Examples:**

* Risk types
* Coverage categories
* Geographic codes

---

#### Document Storage / Archive

* Stores generated insurance documents
* Provides long-term retention and legal access

**Note:** document generation logic stays inside the platform; storage may be external

---

### System Boundary

Inside the Insurance Platform:

* Product and pricing models
* Validation and business rules
* Policy data snapshot
* Document generation logic

Outside the platform:

* User authentication
* Long-term document archiving
* Partner sales applications

---

### High-Level Interaction Flow

1. Sales channel submits data for quote or policy
2. Platform validates data against product rules
3. Pricing logic calculates premium
4. Policy data is fixed and stored
5. Documents are generated and returned or archived

---

### Business Guarantees

The platform guarantees:

* Consistent pricing for the same product version
* Identical documents for identical policy data
* Traceability of how price and documents were formed
* Safe integration with multiple sales channels

---

### What the System Is NOT

* Not a CRM system
* Not a payment system
* Not a document archive
* Not an identity provider

It focuses strictly on **insurance product logic and execution**.
