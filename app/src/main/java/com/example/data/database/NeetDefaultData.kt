package com.example.data.database

import com.example.data.entity.RoutineTask
import com.example.data.entity.SyllabusTopic

object NeetDefaultData {

    fun getDefaultRoutineTasks(date: String): List<RoutineTask> {
        return listOf(
            RoutineTask(
                date = date,
                blockOrder = 1,
                title = "11th Chemistry Physical",
                targetQuestions = 45,
                plannedMinutes = 45,
                chapter = "Some Basic Concepts of Chemistry",
                topic = "Mole Concept & Stoichiometry"
            ),
            RoutineTask(
                date = date,
                blockOrder = 2,
                title = "12th Chemistry Physical",
                targetQuestions = 45,
                plannedMinutes = 45,
                chapter = "Solutions",
                topic = "Concentration Terms & Raoult's Law"
            ),
            RoutineTask(
                date = date,
                blockOrder = 3,
                title = "Organic Chemistry",
                targetQuestions = 30,
                plannedMinutes = 30,
                chapter = "GOC (Basic Principles)",
                topic = "Electronic Effects & Reaction Intermediates"
            ),
            RoutineTask(
                date = date,
                blockOrder = 4,
                title = "Inorganic Chemistry",
                targetQuestions = 30,
                plannedMinutes = 30,
                chapter = "Chemical Bonding & Molecular Structure",
                topic = "VSEPR & Hybridisation"
            ),
            RoutineTask(
                date = date,
                blockOrder = 5,
                title = "11th Physics",
                targetQuestions = 60,
                plannedMinutes = 120,
                chapter = "Kinematics",
                topic = "Motion in a Straight Line & Projectile"
            ),
            RoutineTask(
                date = date,
                blockOrder = 6,
                title = "12th Physics",
                targetQuestions = 60,
                plannedMinutes = 120,
                chapter = "Electrostatics",
                topic = "Coulomb's Law, Electric Field & Gauss's Law"
            ),
            RoutineTask(
                date = date,
                blockOrder = 7,
                title = "11th Zoology",
                targetQuestions = 30,
                plannedMinutes = 30,
                chapter = "Human Physiology",
                topic = "Breathing and Exchange of Gases"
            ),
            RoutineTask(
                date = date,
                blockOrder = 8,
                title = "11th Botany",
                targetQuestions = 30,
                plannedMinutes = 30,
                chapter = "Biological Classification",
                topic = "Monera, Protista & Fungi"
            ),
            RoutineTask(
                date = date,
                blockOrder = 9,
                title = "12th Zoology",
                targetQuestions = 30,
                plannedMinutes = 30,
                chapter = "Human Reproduction",
                topic = "Gametogenesis & Fertilisation"
            ),
            RoutineTask(
                date = date,
                blockOrder = 10,
                title = "12th Botany",
                targetQuestions = 30,
                plannedMinutes = 30,
                chapter = "Molecular Basis of Inheritance",
                topic = "DNA Replication & Genetic Code"
            )
        )
    }

    fun getInitialSyllabusTopics(): List<SyllabusTopic> {
        val list = mutableListOf<SyllabusTopic>()
        var order = 1

        fun add(subj: String, cls: Int, br: String, unt: String, ch: String, top: String, sub: String) {
            list.add(
                SyllabusTopic(
                    subject = subj,
                    classLevel = cls,
                    branch = br,
                    unit = unt,
                    chapter = ch,
                    topic = top,
                    subtopic = sub,
                    orderIndex = order++,
                    status = "NOT_STARTED"
                )
            )
        }

        // ========================================================
        // OFFICIAL NEET UG 2026: CHEMISTRY CLASS 11 (PHYSICAL)
        // ========================================================
        val uChem1 = "Unit I: Some Basic Concepts of Chemistry"
        add("Chemistry", 11, "Physical", uChem1, "Some Basic Concepts of Chemistry", "Matter and its Nature", "Dalton's atomic theory, Concept of atom, molecule, element and compound")
        add("Chemistry", 11, "Physical", uChem1, "Some Basic Concepts of Chemistry", "Atomic and Molecular Masses", "Mole concept, Molar mass, Percentage composition, Empirical and molecular formula")
        add("Chemistry", 11, "Physical", uChem1, "Some Basic Concepts of Chemistry", "Stoichiometry & Chemical Equations", "Chemical equations, Stoichiometry, Limiting reagent calculations")
        add("Chemistry", 11, "Physical", uChem1, "Some Basic Concepts of Chemistry", "Concentration of Solutions", "Molarity, Molality, Mole fraction, Normality")

        val uChem2 = "Unit II: Structure of Atom"
        add("Chemistry", 11, "Physical", uChem2, "Structure of Atom", "Atomic Models & Electromagnetic Radiation", "Bohr's model, Hydrogen spectrum, Dual nature of matter (De Broglie)")
        add("Chemistry", 11, "Physical", uChem2, "Structure of Atom", "Quantum Mechanics & Uncertainty", "Heisenberg uncertainty principle, Significance of ψ and ψ²")
        add("Chemistry", 11, "Physical", uChem2, "Structure of Atom", "Quantum Numbers & Orbitals", "Principal, azimuthal, magnetic and spin quantum numbers, Shapes of s, p and d orbitals")
        add("Chemistry", 11, "Physical", uChem2, "Structure of Atom", "Electronic Configuration Rules", "Aufbau principle, Pauli exclusion principle, Hund's rule, Stability of half/completely filled orbitals")

        val uChem3 = "Unit III: Chemical Thermodynamics"
        add("Chemistry", 11, "Physical", uChem3, "Chemical Thermodynamics", "First Law of Thermodynamics", "Internal energy, Work and heat, State functions, Extensive and intensive properties")
        add("Chemistry", 11, "Physical", uChem3, "Chemical Thermodynamics", "Enthalpy and Thermochemistry", "Heat capacity, Cp and Cv, Enthalpies of bond dissociation, formation, atomization, sublimation")
        add("Chemistry", 11, "Physical", uChem3, "Chemical Thermodynamics", "Hess's Law & Spontaneity", "Hess's law of constant heat summation, Entropy as state function")
        add("Chemistry", 11, "Physical", uChem3, "Chemical Thermodynamics", "Gibbs Free Energy", "ΔG = ΔH - TΔS, Criteria for equilibrium and spontaneity")

        val uChem4 = "Unit IV: Equilibrium"
        add("Chemistry", 11, "Physical", uChem4, "Equilibrium", "Chemical Equilibrium & Le Chatelier", "Dynamic nature, Law of mass action, Kp and Kc, Factors affecting equilibrium")
        add("Chemistry", 11, "Physical", uChem4, "Equilibrium", "Ionic Equilibrium & Acid-Base Concepts", "Arrhenius, Bronsted-Lowry and Lewis concepts, Ionization of acids and bases")
        add("Chemistry", 11, "Physical", uChem4, "Equilibrium", "pH, Buffers & Common Ion Effect", "pH scale, Hydrolysis of salts, Buffer solutions, Henderson equation")
        add("Chemistry", 11, "Physical", uChem4, "Equilibrium", "Solubility Product", "Ksp and precipitation reactions, Applications in qualitative analysis")

        val uChem5 = "Unit V: Redox Reactions"
        add("Chemistry", 11, "Physical", uChem5, "Redox Reactions", "Oxidation Number Concept", "Rules for calculating oxidation states, Types of redox reactions")
        add("Chemistry", 11, "Physical", uChem5, "Redox Reactions", "Balancing Redox Equations", "Ion-electron method, Oxidation number method, Applications in titrations")

        // ========================================================
        // OFFICIAL NEET UG 2026: CHEMISTRY CLASS 11 (INORGANIC)
        // ========================================================
        val uChem6 = "Unit VI: Classification of Elements and Periodicity"
        add("Chemistry", 11, "Inorganic", uChem6, "Classification of Elements", "Modern Periodic Law", "Electronic configurations, Periodic table trends: Atomic radii, Ionic radii")
        add("Chemistry", 11, "Inorganic", uChem6, "Classification of Elements", "Periodic Properties & Trends", "Ionization enthalpy, Electron gain enthalpy, Electronegativity, Valency")

        val uChem7 = "Unit VII: Chemical Bonding and Molecular Structure"
        add("Chemistry", 11, "Inorganic", uChem7, "Chemical Bonding", "Ionic & Covalent Bonding", "Lattice energy, Born-Haber cycle, Lewis structures, Formal charge")
        add("Chemistry", 11, "Inorganic", uChem7, "Chemical Bonding", "VSEPR Theory & Dipole Moment", "Shapes of simple molecules, Dipole moment, Partial ionic character of covalent bonds")
        add("Chemistry", 11, "Inorganic", uChem7, "Chemical Bonding", "Valence Bond Theory & Hybridisation", "Orbital overlap, σ and π bonds, sp, sp², sp³, dsp², sp³d, sp³d² hybridisation")
        add("Chemistry", 11, "Inorganic", uChem7, "Chemical Bonding", "Molecular Orbital Theory & Hydrogen Bonding", "LCAO, Homonuclear diatomic molecules (H2 to O2), Bond order, Inter & Intra H-bonding")

        val uChem8 = "Unit VIII: p-Block Elements (Class 11)"
        add("Chemistry", 11, "Inorganic", uChem8, "p-Block Elements", "Group 13 & 14 General Introduction", "Electronic configuration, Occurrence, Variation of properties, Oxidation states, Anomalous behavior")

        // ========================================================
        // OFFICIAL NEET UG 2026: CHEMISTRY CLASS 11 (ORGANIC)
        // ========================================================
        val uChem9 = "Unit IX: Organic Chemistry - Some Basic Principles (GOC)"
        add("Chemistry", 11, "Organic", uChem9, "General Organic Chemistry", "IUPAC Nomenclature & Isomerism", "IUPAC rules for mono and polyfunctional compounds, Structural and stereoisomerism")
        add("Chemistry", 11, "Organic", uChem9, "General Organic Chemistry", "Electronic Displacements in Covalent Bonds", "Inductive effect, Electromeric effect, Resonance, Hyperconjugation")
        add("Chemistry", 11, "Organic", uChem9, "General Organic Chemistry", "Reactive Intermediates & Fission", "Homolytic & heterolytic fission, Carbocations, Carbanions, Free radicals, Electrophiles & Nucleophiles")

        val uChem10 = "Unit X: Hydrocarbons"
        add("Chemistry", 11, "Organic", uChem10, "Hydrocarbons", "Alkanes", "Preparation, Conformations of ethane (Sawhorse & Newman), Free radical halogenation")
        add("Chemistry", 11, "Organic", uChem10, "Hydrocarbons", "Alkenes", "Geometrical isomerism (cis-trans), Markownikoff & anti-Markownikoff addition, Ozonolysis")
        add("Chemistry", 11, "Organic", uChem10, "Hydrocarbons", "Alkynes", "Acidity of alkynes, Addition reactions of water, halides, polymerisation")
        add("Chemistry", 11, "Organic", uChem10, "Hydrocarbons", "Aromatic Hydrocarbons", "Huckel's rule (4n+2), Electrophilic substitution: Nitration, Halogenation, Sulphonation, Friedel-Crafts")

        // ========================================================
        // OFFICIAL NEET UG 2026: CHEMISTRY CLASS 12 (PHYSICAL)
        // ========================================================
        val uChem11 = "Unit XI: Solutions"
        add("Chemistry", 12, "Physical", uChem11, "Solutions", "Types of Solutions & Solubility", "Henry's law, Solid in liquid solutions, Gas in liquid solutions")
        add("Chemistry", 12, "Physical", uChem11, "Solutions", "Vapour Pressure & Raoult's Law", "Ideal and non-ideal solutions, Positive and negative deviations")
        add("Chemistry", 12, "Physical", uChem11, "Solutions", "Colligative Properties", "Relative lowering of vapour pressure, Elevation of boiling point, Depression of freezing point, Osmotic pressure")
        add("Chemistry", 12, "Physical", uChem11, "Solutions", "Abnormal Molar Mass & Van't Hoff Factor", "Association and dissociation of solutes, Degree of dissociation")

        val uChem12 = "Unit XII: Electrochemistry"
        add("Chemistry", 12, "Physical", uChem12, "Electrochemistry", "Galvanic Cells & Nernst Equation", "Electrode potential, Standard hydrogen electrode, EMF calculation, Equilibrium constant from Nernst")
        add("Chemistry", 12, "Physical", uChem12, "Electrochemistry", "Electrolytic Conductance & Kohlrausch's Law", "Specific, equivalent and molar conductivity, Variation with dilution, Kohlrausch's applications")
        add("Chemistry", 12, "Physical", uChem12, "Electrochemistry", "Batteries & Fuel Cells", "Primary & secondary cells (Lead accumulator), Dry cell, H2-O2 fuel cell, Corrosion")

        val uChem13 = "Unit XIII: Chemical Kinetics"
        add("Chemistry", 12, "Physical", uChem13, "Chemical Kinetics", "Rate of Reaction & Rate Law", "Average and instantaneous rate, Rate constant, Order and molecularity of reaction")
        add("Chemistry", 12, "Physical", uChem13, "Chemical Kinetics", "Integrated Rate Equations", "Zero order and first order reactions, Half-life period (t1/2)")
        add("Chemistry", 12, "Physical", uChem13, "Chemical Kinetics", "Temperature Dependence & Arrhenius Equation", "Concept of activation energy (Ea), Arrhenius equation, Catalyst effect, Collision theory")

        // ========================================================
        // OFFICIAL NEET UG 2026: CHEMISTRY CLASS 12 (INORGANIC)
        // ========================================================
        val uChem14 = "Unit XIV: The d- and f-Block Elements"
        add("Chemistry", 12, "Inorganic", uChem14, "d and f Block Elements", "Transition Elements (3d series)", "Electronic configuration, Metallic character, Ionization enthalpies, Oxidation states, Catalytic properties")
        add("Chemistry", 12, "Inorganic", uChem14, "d and f Block Elements", "Magnetic Properties & Colored Ions", "Interstitial compounds, Alloy formation, Preparation and properties of K2Cr2O7 and KMnO4")
        add("Chemistry", 12, "Inorganic", uChem14, "d and f Block Elements", "Lanthanoids & Actinoids", "Lanthanoid contraction and consequences, Oxidation states, Comparison with actinoids")

        val uChem15 = "Unit XV: Coordination Compounds"
        add("Chemistry", 12, "Inorganic", uChem15, "Coordination Compounds", "Werner's Theory & IUPAC Rules", "Ligands, Coordination number, Denticity, Chelation, IUPAC nomenclature of mononuclear complexes")
        add("Chemistry", 12, "Inorganic", uChem15, "Coordination Compounds", "Isomerism in Coordination Complexes", "Geometrical, Optical, Ionisation, Solvate, Linkage and Coordination isomerism")
        add("Chemistry", 12, "Inorganic", uChem15, "Coordination Compounds", "Bonding in Complexes (VBT & CFT)", "Crystal Field Theory: Splitting in octahedral and tetrahedral fields, CFSE, Magnetic nature, Color")

        // ========================================================
        // OFFICIAL NEET UG 2026: CHEMISTRY CLASS 12 (ORGANIC)
        // ========================================================
        val uChem16 = "Unit XVI: Haloalkanes and Haloarenes"
        add("Chemistry", 12, "Organic", uChem16, "Haloalkanes and Haloarenes", "Nomenclature & SN1 vs SN2 Mechanisms", "Nucleophilic substitution mechanisms (SN1, SN2), Stereochemical aspects, Inversion of configuration")
        add("Chemistry", 12, "Organic", uChem16, "Haloalkanes and Haloarenes", "Reactions of Haloalkanes & Haloarenes", "Elimination reactions (Saytzeff rule), Grignard reagent, Electrophilic substitution in haloarenes")

        val uChem17 = "Unit XVII: Alcohols, Phenols and Ethers"
        add("Chemistry", 12, "Organic", uChem17, "Alcohols, Phenols and Ethers", "Alcohols", "Hydroboration-oxidation, Reduction of carbonyl compounds, Lucas test, Dehydration mechanism")
        add("Chemistry", 12, "Organic", uChem17, "Alcohols, Phenols and Ethers", "Phenols", "Acidity of phenols, Electrophilic substitution: Reimer-Tiemann, Kolbe's reaction, Oxidation")
        add("Chemistry", 12, "Organic", uChem17, "Alcohols, Phenols and Ethers", "Ethers", "Williamson ether synthesis, Reaction with halogen acids (HI cleavage)")

        val uChem18 = "Unit XVIII: Aldehydes, Ketones and Carboxylic Acids"
        add("Chemistry", 12, "Organic", uChem18, "Aldehydes, Ketones and Carboxylic Acids", "Carbonyl Nucleophilic Additions", "Mechanism of nucleophilic addition, HCN, NaHSO3, Grignard reagents, Ammonia derivatives")
        add("Chemistry", 12, "Organic", uChem18, "Aldehydes, Ketones and Carboxylic Acids", "Reactions Due to α-Hydrogen", "Aldol condensation, Cross aldol condensation, Cannizzaro reaction, Haloform reaction")
        add("Chemistry", 12, "Organic", uChem18, "Aldehydes, Ketones and Carboxylic Acids", "Carboxylic Acids", "Acidity and inductive effect, Hell-Volhard-Zelinsky (HVZ) reaction, Decarboxylation")

        val uChem19 = "Unit XIX: Organic Compounds Containing Nitrogen"
        add("Chemistry", 12, "Organic", uChem19, "Amines and Diazonium Salts", "Amines Structure & Basicity", "Classification, Basicity in gaseous vs aqueous phase, Hofmann bromamide degradation")
        add("Chemistry", 12, "Organic", uChem19, "Amines and Diazonium Salts", "Reactions of Amines & Diazonium Salts", "Carbylamine test, Hinsberg test, Diazotisation, Sandmeyer and coupling reactions")

        val uChem20 = "Unit XX: Biomolecules"
        add("Chemistry", 12, "Organic", uChem20, "Biomolecules", "Carbohydrates", "Classification, Monosaccharides (Glucose and Fructose), Glycosidic linkage, Reducing sugars")
        add("Chemistry", 12, "Organic", uChem20, "Biomolecules", "Proteins & Nucleic Acids", "Amino acids, Peptide bond, Primary/Secondary/Tertiary structures, DNA and RNA structure")

        // ========================================================
        // OFFICIAL NEET UG 2026: PHYSICS CLASS 11
        // ========================================================
        val uPhys1 = "Unit I: Physics and Measurement"
        add("Physics", 11, "General", uPhys1, "Units and Measurements", "Dimensions of Physical Quantities", "Dimensional analysis, Conversion of units, Dimensional formula verification")
        add("Physics", 11, "General", uPhys1, "Units and Measurements", "Errors in Measurement", "Systematic & random errors, Least count, Combination of errors, Significant figures")

        val uPhys2 = "Unit II: Kinematics"
        add("Physics", 11, "General", uPhys2, "Motion in a Straight Line", "Kinematic Variables & Equations", "Position, displacement, speed, velocity, Uniform acceleration equations, Relative velocity")
        add("Physics", 11, "General", uPhys2, "Motion in a Straight Line", "Graphs & Motion Under Gravity", "x-t, v-t and a-t graphs, Free fall, Stopping distance")
        add("Physics", 11, "General", uPhys2, "Motion in a Plane", "Vectors & Resolution", "Scalar and vector products, Unit vectors, Vector addition (Triangle and Parallelogram law)")
        add("Physics", 11, "General", uPhys2, "Motion in a Plane", "Projectile Motion & Circular Motion", "Trajectory equation, Maximum height, Range, Time of flight, Uniform circular motion")

        val uPhys3 = "Unit III: Laws of Motion"
        add("Physics", 11, "General", uPhys3, "Laws of Motion", "Newton's Laws & Impulse", "Inertia, Momentum, Newton's 2nd & 3rd laws, Impulse-momentum theorem, Free body diagrams")
        add("Physics", 11, "General", uPhys3, "Laws of Motion", "Friction & Dynamics of Circular Motion", "Static and kinetic friction, Laws of friction, Centripetal force, Banking of roads")

        val uPhys4 = "Unit IV: Work, Energy and Power"
        add("Physics", 11, "General", uPhys4, "Work, Energy and Power", "Work-Energy Theorem", "Work done by constant & variable force, Kinetic energy, Work-energy relation")
        add("Physics", 11, "General", uPhys4, "Work, Energy and Power", "Conservation of Energy & Power", "Conservative forces, Potential energy of spring, Conservation of mechanical energy, Power")
        add("Physics", 11, "General", uPhys4, "Work, Energy and Power", "Collisions in 1D and 2D", "Elastic and inelastic collisions, Coefficient of restitution (e)")

        val uPhys5 = "Unit V: Rotational Motion"
        add("Physics", 11, "General", uPhys5, "Rotational Motion", "Centre of Mass", "Discrete & continuous mass distributions, Velocity and acceleration of COM")
        add("Physics", 11, "General", uPhys5, "Rotational Motion", "Torque and Angular Momentum", "Torque = r x F, Angular momentum conservation, Equilibrium of rigid body")
        add("Physics", 11, "General", uPhys5, "Rotational Motion", "Moment of Inertia", "Radius of gyration, Parallel and perpendicular axes theorems, MOI of ring, disc, rod, cylinder, sphere")

        val uPhys6 = "Unit VI: Gravitation"
        add("Physics", 11, "General", uPhys6, "Gravitation", "Kepler's Laws & Universal Law", "Kepler's three laws, Gravitational constant, Acceleration due to gravity (g)")
        add("Physics", 11, "General", uPhys6, "Gravitation", "Variation of 'g' & Satellite Motion", "Variation with altitude and depth, Gravitational potential energy, Escape speed, Orbital speed")

        val uPhys7 = "Unit VII: Properties of Solids and Liquids"
        add("Physics", 11, "General", uPhys7, "Mechanical Properties", "Elasticity & Hooke's Law", "Stress-strain curve, Young's modulus, Bulk modulus, Shear modulus")
        add("Physics", 11, "General", uPhys7, "Mechanical Properties", "Hydrostatics & Pascal's Law", "Fluid pressure, Buoyancy, Archimedes' principle, Hydraulic lift")
        add("Physics", 11, "General", uPhys7, "Mechanical Properties", "Hydrodynamics & Viscosity", "Bernoulli's theorem, Continuity equation, Stokes' law, Terminal velocity, Surface tension")

        val uPhys8 = "Unit VIII: Thermodynamics"
        add("Physics", 11, "General", uPhys8, "Thermal Physics & Thermodynamics", "Thermal Expansion & Calorimetry", "Specific heat, Latent heat, Heat transfer (Conduction, Convection, Radiation, Wien's law, Stefan's law)")
        add("Physics", 11, "General", uPhys8, "Thermal Physics & Thermodynamics", "First & Second Law of Thermodynamics", "Isothermal, Adiabatic, Isochoric, Isobaric processes, Work done, Carnot engine efficiency")

        val uPhys9 = "Unit IX: Kinetic Theory of Gases"
        add("Physics", 11, "General", uPhys9, "Kinetic Theory of Gases", "Gas Laws & Molecular Speeds", "Equation of state, RMS velocity, Average velocity, Most probable speed")
        add("Physics", 11, "General", uPhys9, "Kinetic Theory of Gases", "Degrees of Freedom & Mean Free Path", "Law of equipartition of energy, Specific heats of mono/di/polyatomic gases, Mean free path")

        val uPhys10 = "Unit X: Oscillations and Waves"
        add("Physics", 11, "General", uPhys10, "Oscillations and Waves", "Simple Harmonic Motion (SHM)", "Equation of SHM, Velocity, Acceleration, Energy in SHM, Simple pendulum, Spring-mass system")
        add("Physics", 11, "General", uPhys10, "Oscillations and Waves", "Wave Motion & Sound", "Transverse and longitudinal waves, Speed of sound (Laplace correction), Standing waves in strings and pipes")
        add("Physics", 11, "General", uPhys10, "Oscillations and Waves", "Beats and Doppler Effect", "Principle of superposition, Beat frequency, Doppler effect for sound")

        // ========================================================
        // OFFICIAL NEET UG 2026: PHYSICS CLASS 12
        // ========================================================
        val uPhys11 = "Unit XI: Electrostatics"
        add("Physics", 12, "General", uPhys11, "Electrostatics", "Coulomb's Law & Electric Field", "Electric charge conservation, Coulomb's force in vector form, Electric field of dipole (axial & equatorial)")
        add("Physics", 12, "General", uPhys11, "Electrostatics", "Gauss's Law and Applications", "Electric flux, Gauss's law, Field due to infinite wire, plane sheet, spherical shell")
        add("Physics", 12, "General", uPhys11, "Electrostatics", "Electric Potential & Equipotential Surfaces", "Potential due to point charge & dipole, Potential energy of system of charges")
        add("Physics", 12, "General", uPhys11, "Electrostatics", "Capacitors & Dielectrics", "Parallel plate capacitor, Series and parallel combinations, Energy stored in capacitor")

        val uPhys12 = "Unit XII: Current Electricity"
        add("Physics", 12, "General", uPhys12, "Current Electricity", "Ohm's Law & Drift Velocity", "Drift speed, Mobility, Resistance and resistivity, Temperature dependence of resistance")
        add("Physics", 12, "General", uPhys12, "Current Electricity", "Kirchhoff's Laws & Circuit Analysis", "Junction rule, Loop rule, Wheatstone bridge, Metre bridge, Potentiometer principles")

        val uPhys13 = "Unit XIII: Magnetic Effects of Current"
        add("Physics", 12, "General", uPhys13, "Magnetism", "Biot-Savart Law & Ampere's Law", "Magnetic field of straight wire, circular loop, Solenoid and toroid")
        add("Physics", 12, "General", uPhys13, "Magnetism", "Force on Moving Charges & Galvanometer", "Lorentz force, Motion in magnetic field, Cyclotron, Moving coil galvanometer conversion to ammeter/voltmeter")
        add("Physics", 12, "General", uPhys13, "Magnetism", "Magnetism and Matter", "Current loop as magnetic dipole, Bar magnet, Dia-, para-, and ferromagnetic substances")

        val uPhys14 = "Unit XIV: EMI and AC"
        add("Physics", 12, "General", uPhys14, "EMI and Alternating Current", "Faraday's & Lenz's Law", "Induced EMF, Motional EMF, Self-inductance, Mutual inductance")
        add("Physics", 12, "General", uPhys14, "EMI and Alternating Current", "Alternating Current & LCR Circuit", "RMS and peak values, Inductive and capacitive reactance, LCR series resonance, Power factor, Transformer")

        val uPhys15 = "Unit XV: Electromagnetic Waves"
        add("Physics", 12, "General", uPhys15, "Electromagnetic Waves", "EM Spectrum & Characteristics", "Displacement current, Transverse nature of EM waves, Radio, microwave, IR, visible, UV, X-ray, Gamma rays")

        val uPhys16 = "Unit XVI: Optics"
        add("Physics", 12, "General", uPhys16, "Ray Optics", "Refraction & Total Internal Reflection", "Mirrors, Snell's law, TIR and optical fibres, Prisms and dispersion")
        add("Physics", 12, "General", uPhys16, "Ray Optics", "Lenses & Optical Instruments", "Lens maker's formula, Compound microscope, Astronomical telescope")
        add("Physics", 12, "General", uPhys16, "Wave Optics", "Interference & Diffraction", "Huygens' principle, Young's double slit experiment (YDSE), Single slit diffraction, Polarisation")

        val uPhys17 = "Unit XVII: Dual Nature of Matter"
        add("Physics", 12, "General", uPhys17, "Modern Physics", "Photoelectric Effect", "Hertz and Lenard's observations, Einstein's photoelectric equation, Work function, Stopping potential")
        add("Physics", 12, "General", uPhys17, "Modern Physics", "Matter Waves", "De Broglie wavelength of electron, Davisson-Germer experiment")

        val uPhys18 = "Unit XVIII: Atoms and Nuclei"
        add("Physics", 12, "General", uPhys18, "Atoms and Nuclei", "Atomic Models & Hydrogen Spectrum", "Rutherford model, Bohr's postulates, Energy levels of hydrogen atom")
        add("Physics", 12, "General", uPhys18, "Atoms and Nuclei", "Nuclear Physics", "Mass defect, Binding energy per nucleon curve, Nuclear fission and fusion")

        val uPhys19 = "Unit XIX: Electronic Devices"
        add("Physics", 12, "General", uPhys19, "Semiconductors", "p-n Junction Diode", "Energy bands in conductors/insulators/semiconductors, Forward and reverse bias, Half-wave and full-wave rectifier")
        add("Physics", 12, "General", uPhys19, "Semiconductors", "Special Diodes & Logic Gates", "Zener diode as voltage regulator, LED, Photodiode, Solar cell, Basic logic gates (OR, AND, NOT, NAND, NOR)")

        // ========================================================
        // OFFICIAL NEET UG 2026: BIOLOGY CLASS 11 (BOTANY & ZOOLOGY)
        // ========================================================
        val uBio1 = "Unit I: Diversity in Living World"
        add("Biology", 11, "Botany", uBio1, "The Living World", "Taxonomy & Hierarchy", "What is living?, Biodiversity, Binomial nomenclature, Taxonomic hierarchy")
        add("Biology", 11, "Botany", uBio1, "Biological Classification", "Monera & Protista", "Five kingdom classification, Archaebacteria, Eubacteria, Protozoa, Chrysophytes, Dinoflagellates")
        add("Biology", 11, "Botany", uBio1, "Biological Classification", "Fungi & Viruses", "Phycomycetes, Ascomycetes, Basidiomycetes, Deuteromycetes, Viruses, Viroids, Prions, Lichens")
        add("Biology", 11, "Botany", uBio1, "Plant Kingdom", "Algae, Bryophytes & Pteridophytes", "Classification, Life cycle patterns, Alternation of generation")
        add("Biology", 11, "Botany", uBio1, "Plant Kingdom", "Gymnosperms & Angiosperms", "Salient features, Seeds, Conifers, Double fertilization overview")
        add("Biology", 11, "Zoology", uBio1, "Animal Kingdom", "Non-Chordates", "Levels of organisation, Symmetry, Coelom, Porifera, Coelenterata, Platyhelminthes, Aschelminthes, Annelida, Arthropoda, Mollusca, Echinodermata")
        add("Biology", 11, "Zoology", uBio1, "Animal Kingdom", "Chordates", "Protochordata, Cyclostomata, Chondrichthyes, Osteichthyes, Amphibia, Reptilia, Aves, Mammalia")

        val uBio2 = "Unit II: Structural Organisation in Animals and Plants"
        add("Biology", 11, "Botany", uBio2, "Morphology of Flowering Plants", "Roots, Stems & Leaves", "Modifications, Inflorescence, Flower parts, Placentation, Fruit and seed types")
        add("Biology", 11, "Botany", uBio2, "Anatomy of Flowering Plants", "Plant Tissues", "Meristematic and permanent tissues, Monocot and dicot root, stem and leaf anatomy")
        add("Biology", 11, "Zoology", uBio2, "Structural Organisation in Animals", "Animal Tissues & Frog", "Epithelial, Connective, Muscular, Neural tissues, Morphology, anatomy and digestive/circulatory/reproductive system of Frog")

        val uBio3 = "Unit III: Cell Structure and Function"
        add("Biology", 11, "Botany", uBio3, "Cell: The Unit of Life", "Cell Theory & Prokaryotes", "Cell envelope, Ribosomes, Inclusion bodies")
        add("Biology", 11, "Botany", uBio3, "Cell: The Unit of Life", "Eukaryotic Organelles", "Plasma membrane (Fluid mosaic model), ER, Golgi, Lysosomes, Mitochondria, Chloroplasts, Nucleus")
        add("Biology", 11, "Zoology", uBio3, "Biomolecules", "Structure of Biomolecules", "Proteins, Carbohydrates, Lipids, Nucleic acids, Enzymes: Classification and mechanism")
        add("Biology", 11, "Botany", uBio3, "Cell Cycle and Division", "Mitosis and Meiosis", "Interphase (G1, S, G2), Prophase, Metaphase, Anaphase, Telophase, Significance of crossing over")

        val uBio4 = "Unit IV: Plant Physiology"
        add("Biology", 11, "Botany", uBio4, "Photosynthesis in Higher Plants", "Light Reactions & Photophosphorylation", "Photosynthetic pigments, Cyclic and non-cyclic photophosphorylation, Z-scheme")
        add("Biology", 11, "Botany", uBio4, "Photosynthesis in Higher Plants", "Dark Reactions (Calvin Cycle & C4)", "C3 pathway, C4 pathway, Photorespiration, Factors affecting photosynthesis")
        add("Biology", 11, "Botany", uBio4, "Respiration in Plants", "Glycolysis & Fermentation", "Aerobic and anaerobic pathways, EMP pathway, Lactic acid and alcoholic fermentation")
        add("Biology", 11, "Botany", uBio4, "Respiration in Plants", "Krebs Cycle & ETS", "Citric acid cycle, Electron transport system, Oxidative phosphorylation, Respiratory quotient")
        add("Biology", 11, "Botany", uBio4, "Plant Growth and Development", "Plant Hormones (PGRs)", "Auxin, Gibberellin, Cytokinin, Ethylene, Abscisic acid, Photoperiodism, Differentiation")

        val uBio5 = "Unit V: Human Physiology"
        add("Biology", 11, "Zoology", uBio5, "Breathing and Exchange of Gases", "Mechanism of Breathing", "Inspiration, Expiration, Respiratory volumes and capacities")
        add("Biology", 11, "Zoology", uBio5, "Breathing and Exchange of Gases", "Gas Transport & Regulation", "Oxygen dissociation curve, Transport of CO2, Disorders (Asthma, Emphysema)")
        add("Biology", 11, "Zoology", uBio5, "Body Fluids and Circulation", "Blood, Lymph & Cardiac Cycle", "Formed elements, ABO & Rh grouping, Cardiac cycle, Heart sounds, ECG")
        add("Biology", 11, "Zoology", uBio5, "Body Fluids and Circulation", "Double Circulation & Blood Pressure", "Coronary circulation, Hypertension, Angina, Heart failure")
        add("Biology", 11, "Zoology", uBio5, "Excretory Products and Elimination", "Urine Formation", "Nephron structure, Glomerular filtration, Reabsorption, Secretion, Counter current system")
        add("Biology", 11, "Zoology", uBio5, "Excretory Products and Elimination", "Regulation of Kidney Function", "RAAS pathway, ADH, ANF, Micturition, Renal failure and dialysis")
        add("Biology", 11, "Zoology", uBio5, "Locomotion and Movement", "Skeletal System & Muscle Contraction", "Sliding filament theory, Actin-Myosin, Axial and appendicular skeleton, Joints")
        add("Biology", 11, "Zoology", uBio5, "Neural Control and Coordination", "Nerve Impulse & Reflex Action", "Generation and conduction of impulse, Synaptic transmission, Human brain parts")
        add("Biology", 11, "Zoology", uBio5, "Chemical Coordination and Integration", "Endocrine Glands and Hormones", "Hypothalamus, Pituitary, Thyroid, Parathyroid, Adrenal, Pancreas, Gonads, Mechanism of hormone action")

        // ========================================================
        // OFFICIAL NEET UG 2026: BIOLOGY CLASS 12 (BOTANY & ZOOLOGY)
        // ========================================================
        val uBio6 = "Unit VI: Reproduction"
        add("Biology", 12, "Botany", uBio6, "Sexual Reproduction in Flowering Plants", "Pollen & Embryo Sac Development", "Microsporogenesis, Megasporogenesis, Pollination mechanisms, Outbreeding devices")
        add("Biology", 12, "Botany", uBio6, "Sexual Reproduction in Flowering Plants", "Double Fertilization & Seed", "Endosperm, Embryo development, Seed dormancy, Apomixis, Polyembryony")
        add("Biology", 12, "Zoology", uBio6, "Human Reproduction", "Male and Female Reproductive Systems", "Testis and Ovary anatomy, Spermatogenesis, Oogenesis, Hormonal regulation")
        add("Biology", 12, "Zoology", uBio6, "Human Reproduction", "Menstrual Cycle & Embryonic Development", "Phases of menstrual cycle, Fertilisation, Cleavage, Blastocyst, Implantation, Parturition")
        add("Biology", 12, "Zoology", uBio6, "Reproductive Health", "Contraception & ART", "Population explosion, Birth control methods, MTP, STIs, Infertility, IVF, ZIFT, GIFT")

        val uBio7 = "Unit VII: Genetics and Evolution"
        add("Biology", 12, "Botany", uBio7, "Principles of Inheritance and Variation", "Mendelian Genetics", "Monohybrid cross, Dihybrid cross, Incomplete dominance, Codominance, Multiple alleles")
        add("Biology", 12, "Botany", uBio7, "Principles of Inheritance and Variation", "Chromosomal Theory & Genetic Disorders", "Linkage and recombination, Sex determination, Pedigree analysis, Mendelian & Chromosomal disorders")
        add("Biology", 12, "Botany", uBio7, "Molecular Basis of Inheritance", "DNA Structure & Replication", "Double helix structure, DNA packaging in chromatin, Meselson-Stahl experiment")
        add("Biology", 12, "Botany", uBio7, "Molecular Basis of Inheritance", "Transcription, Translation & Genetic Code", "RNA types, Transcription unit, Genetic code properties, Translation process")
        add("Biology", 12, "Botany", uBio7, "Molecular Basis of Inheritance", "Gene Regulation & DNA Fingerprinting", "Lac Operon, Human Genome Project (HGP), DNA fingerprinting technique")
        add("Biology", 12, "Zoology", uBio7, "Evolution", "Origin of Life & Evidence", "Miller-Urey experiment, Homologous and analogous organs, Embryological evidence")
        add("Biology", 12, "Zoology", uBio7, "Evolution", "Mechanism of Evolution & Hardy-Weinberg", "Natural selection types, Gene flow, Genetic drift, Hardy-Weinberg equilibrium, Human evolution")

        val uBio8 = "Unit VIII: Biology in Human Welfare"
        add("Biology", 12, "Zoology", uBio8, "Human Health and Disease", "Pathogens & Common Infectious Diseases", "Typhoid, Pneumonia, Malaria (Plasmodium life cycle), Amoebiasis, Filariasis, Ringworm")
        add("Biology", 12, "Zoology", uBio8, "Human Health and Disease", "Immunity, AIDS & Cancer", "Innate and acquired immunity, Vaccination, Allergies, HIV replication, Benign vs malignant tumors")
        add("Biology", 12, "Botany", uBio8, "Microbes in Human Welfare", "Microbes in Household & Industry", "Fermentation, Antibiotics, Sewage treatment plant, Biogas production, Biofertilizers")

        val uBio9 = "Unit IX: Biotechnology"
        add("Biology", 12, "Botany", uBio9, "Biotechnology: Principles and Processes", "Genetic Engineering Tools", "Restriction enzymes (Endonucleases), DNA ligases, Cloning vectors (pBR322), Competent host")
        add("Biology", 12, "Botany", uBio9, "Biotechnology: Principles and Processes", "Processes of Recombinant DNA", "Isolation of DNA, PCR (Polymerase Chain Reaction), Gel electrophoresis, Bioreactors")
        add("Biology", 12, "Zoology", uBio9, "Biotechnology and its Applications", "Applications in Agriculture & Medicine", "Bt cotton, RNA interference (RNAi), Genetically engineered Insulin, Gene therapy, Transgenic animals")

        val uBio10 = "Unit X: Ecology and Environment"
        add("Biology", 12, "Botany", uBio10, "Organisms and Populations", "Adaptations & Population Attributes", "Abiotic factors, Adaptations, Birth rate, Death rate, Sex ratio, Age pyramids, Logistic growth")
        add("Biology", 12, "Zoology", uBio10, "Organisms and Populations", "Population Interactions", "Mutualism, Competition, Predation, Parasitism, Commensalism, Amensalism")
        add("Biology", 12, "Botany", uBio10, "Ecosystem", "Structure and Function", "Productivity (GPP, NPP), Decomposition, Energy flow (10% law), Ecological pyramids")
        add("Biology", 12, "Botany", uBio10, "Biodiversity and Conservation", "Patterns & Loss of Biodiversity", "Latitudinal gradients, Species-Area relationship, The Evil Quartet, In-situ and Ex-situ conservation")

        return list
    }
}
